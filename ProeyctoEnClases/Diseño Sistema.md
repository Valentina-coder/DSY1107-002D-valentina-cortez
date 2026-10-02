**Diseño de arquitectura gestión de pedidos y pagos**  
Sistema de mensajería asíncrona/ síncrona

- Contexto

El sistema resuelve el flujo principal de compra en un entorno de comercio electrónico. Cuando el cliente realiza un pedido, el sistema necesita confirmar de forma inmediata si la transacción de pago fue realizada correctamente para informar al usuario en pantalla.  
Tras tareas secundarias como la emisión del comprobante digital y el envío de notificaciones de confirmación al cliente no requieren congelar o demorar la respuesta de compra en la interfaz del usuario 

- Responsabilidades principales:

1. ms-pedidos(Producer):Es el encargado de recibir la solicitud de compra, coordinar el lujo y registrar la orden en la base de datos.  
2. ms-pasarela-pagos(Proveedor Síncrono): Encargado de procesar la transacción monetaria directamente con la entidad financiera  
3. ms-facturación(Consumer 1 Asíncrono): Encargado de generar el comprobante de compra.  
4. ms-notificaciones(Consumer 2 Asíncrono): Encargado de redactar y enviar al correo electrónico la confirmación al cliente.

- Descripción del Flujo Principal:

1. El cliente envía una solicitud Post /pedidos a ms-pedidos  
2. ms-pedidos realiza una llamada síncrona (REST/HTTP) a ms-pasarela-pagos para cobrar la orden  
3. Una vez aprobada la transacción, ms-pedidos registra la orden, pública de manera asíncrona el evento PedidoPagado hacia RabbitMQ y responde inmediatamente HTTP 201 Created al cliente.   
4. RabbitMQ distribuye el evento en paralelo hacia dos colas independientes.   
5. `ms-facturacion` y `ms-notificaciones` consumen el mensaje de sus respectivas colas y ejecutan sus tareas sin afectar al usuario. 

- Identificación y Justificación de Comunicaciones : 

Comunicacion Sincrona  
Origen: ms.pedidos \-\> destino : ms.pasarela.pagos  
tipo: http rest(sincrono)  
Justificación: Al requerir una respuesta inmediata para saber si el cobro fue autorizado o rechazado. El cliente no puede recibir una confirmación de compra si el pago no fue autorizado o validado en tiempo real.

Comunicación Asincronas (RabbitMQ)  
Origen: ms-pedidos-\>RabbitMQ-\> ms-facturacion  
origen: ms-pedidos-\> RabbitMQ-\> ms-notificaciones  
Tipo: Event-driven (Asíncrono)  
Justificación: Tolerancia a fallos, si la api de facturacion estan caidos, el pedido del usuario igual se completa y el mensaje se procesa al recuperarse el servicio.

- Especificación de Mensajería en RabbitMQ 

| Elemento | Configuracion/ Nombre |
| :---- | :---- |
| Evento/Mensaje | PedidoPagado |
| Producer | ms-pedidos |
| Exchange | pedidos.exchange |
| Routing Key | pedido.pagado |
| Consummers | ms-facturacion ms-notificaciones |
| Queues | facturacion.pedido-pagado.queue |
| Bindings | Ambos usan la routing key pedido.pagado conectada a pedidos.exchange |

- Payload Mínimo Esperado (JSON)   
  Evento: PedidoPagado


{  
  "evento": "PedidoPagado",  
  "timestamp": "2026-10-02T13:00:00Z",  
  "data": {  
    "pedidoId": "PED-98234",  
    "clienteId": "USR-5512",  
    "emailCliente": "cliente@ejemplo.cl",  
    "montoTotal": 29990,  
    "metodoPago": "TARJETA\_CREDITO"  
  }  
}

1. Diagrama de Arquitectura y flujo

                             
                               [ Cliente ]
                                      |
                                      | 1. POST /pedidos (Síncrono)
                                      v
                             +---------------+
                             |  ms-pedidos   |
                             +---------------+
                                   /           \
                 2. HTTP REST     /             \ 3. Evento: PedidoPagado
                  (Síncrono)     /                \    (Asíncrono)
                                 v                  v
                   +-------------------+     +------------------+
                   | ms-pasarela-pagos |     | pedidos.exchange | (RabbitMQ)
                    +-------------------+     +------------------+
                                        /          \
                          Routing Key  /            \ Routing Key:
                     "pedido.pagado"  /              \ "pedido.pagado"
                                     v                v
                           +---------------+  +---------------+
                            | Queue Factura |  | Queue Notif.  |
                            +---------------+  +---------------+
                                      |                   |
                                      v                  v
                             +---------------+  +-------------------+
                             |ms-facturacion |  | ms-notificaciones |
                             +---------------+  +-------------------+
                               (Consumer 1)         (Consumer 2)
