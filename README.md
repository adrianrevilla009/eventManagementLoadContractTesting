# eventManagementLoadContractTesting
Event management app using load and contract testing tools

El problema es que lo que tarda el orm en hacer el save y get del ticket + la comprobacion de si el evento tiene mas de 50 tickets ,
es mayor al tiempo en el que entran las peticiones. 
Por tanto, segun van entrando las requests de los tickets empieza a descuadrarse comparado con el numero que se ha guardado.

Si ponemos un contador de peticiones en el servicio EventService en el metodo bookTicket, y metemos un log veremos lo siguiente:

2023-02-05 16:36:38.593  INFO 126012 --- [nio-8443-exec-5] e.c.m.p.event.EventService               : ******** current capacity 1 number request 1
2023-02-05 16:36:38.619  INFO 126012 --- [nio-8443-exec-3] e.c.m.p.event.EventService               : ******** current capacity 2 number request 2
2023-02-05 16:36:38.677  INFO 126012 --- [nio-8443-exec-2] e.c.m.p.event.EventService               : ******** current capacity 3 number request 3
2023-02-05 16:36:38.695  INFO 126012 --- [nio-8443-exec-7] e.c.m.p.event.EventService               : ******** current capacity 4 number request 4
2023-02-05 16:36:38.744  INFO 126012 --- [nio-8443-exec-5] e.c.m.p.event.EventService               : ******** current capacity 5 number request 5
2023-02-05 16:36:38.747  INFO 126012 --- [io-8443-exec-10] e.c.m.p.event.EventService               : ******** current capacity 5 number request 6
2023-02-05 16:36:38.826  INFO 126012 --- [nio-8443-exec-4] e.c.m.p.event.EventService               : ******** current capacity 6 number request 7
2023-02-05 16:36:38.834  INFO 126012 --- [nio-8443-exec-8] e.c.m.p.event.EventService               : ******** current capacity 7 number request 8
2023-02-05 16:36:38.846  INFO 126012 --- [nio-8443-exec-7] e.c.m.p.event.EventService               : ******** current capacity 8 number request 9
2023-02-05 16:36:38.886  INFO 126012 --- [io-8443-exec-10] e.c.m.p.event.EventService               : ******** current capacity 9 number request 10
2023-02-05 16:36:38.908  INFO 126012 --- [nio-8443-exec-6] e.c.m.p.event.EventService               : ******** current capacity 10 number request 11
2023-02-05 16:36:38.909  INFO 126012 --- [nio-8443-exec-2] e.c.m.p.event.EventService               : ******** current capacity 10 number request 12
2023-02-05 16:36:38.961  INFO 126012 --- [nio-8443-exec-7] e.c.m.p.event.EventService               : ******** current capacity 11 number request 13
2023-02-05 16:36:38.969  INFO 126012 --- [nio-8443-exec-1] e.c.m.p.event.EventService               : ******** current capacity 12 number request 14
2023-02-05 16:36:38.974  INFO 126012 --- [nio-8443-exec-8] e.c.m.p.event.EventService               : ******** current capacity 13 number request 15
2023-02-05 16:36:39.010  INFO 126012 --- [io-8443-exec-10] e.c.m.p.event.EventService               : ******** current capacity 14 number request 16
2023-02-05 16:36:39.035  INFO 126012 --- [nio-8443-exec-2] e.c.m.p.event.EventService               : ******** current capacity 15 number request 17
2023-02-05 16:36:39.041  INFO 126012 --- [nio-8443-exec-4] e.c.m.p.event.EventService               : ******** current capacity 16 number request 18
2023-02-05 16:36:39.051  INFO 126012 --- [nio-8443-exec-5] e.c.m.p.event.EventService               : ******** current capacity 17 number request 19
2023-02-05 16:36:39.057  INFO 126012 --- [nio-8443-exec-3] e.c.m.p.event.EventService               : ******** current capacity 17 number request 20
2023-02-05 16:36:39.096  INFO 126012 --- [nio-8443-exec-9] e.c.m.p.event.EventService               : ******** current capacity 18 number request 21
2023-02-05 16:36:39.097  INFO 126012 --- [nio-8443-exec-1] e.c.m.p.event.EventService               : ******** current capacity 18 number request 22
2023-02-05 16:36:39.100  INFO 126012 --- [nio-8443-exec-6] e.c.m.p.event.EventService               : ******** current capacity 19 number request 23
2023-02-05 16:36:39.116  INFO 126012 --- [nio-8443-exec-8] e.c.m.p.event.EventService               : ******** current capacity 20 number request 24
2023-02-05 16:36:39.134  INFO 126012 --- [io-8443-exec-10] e.c.m.p.event.EventService               : ******** current capacity 21 number request 25
2023-02-05 16:36:39.151  INFO 126012 --- [nio-8443-exec-2] e.c.m.p.event.EventService               : ******** current capacity 22 number request 26
2023-02-05 16:36:39.173  INFO 126012 --- [nio-8443-exec-4] e.c.m.p.event.EventService               : ******** current capacity 23 number request 27
2023-02-05 16:36:39.175  INFO 126012 --- [nio-8443-exec-7] e.c.m.p.event.EventService               : ******** current capacity 23 number request 28
2023-02-05 16:36:39.178  INFO 126012 --- [nio-8443-exec-5] e.c.m.p.event.EventService               : ******** current capacity 24 number request 29
2023-02-05 16:36:39.205  INFO 126012 --- [nio-8443-exec-3] e.c.m.p.event.EventService               : ******** current capacity 25 number request 30
2023-02-05 16:36:39.238  INFO 126012 --- [nio-8443-exec-8] e.c.m.p.event.EventService               : ******** current capacity 26 number request 31
2023-02-05 16:36:39.242  INFO 126012 --- [nio-8443-exec-6] e.c.m.p.event.EventService               : ******** current capacity 27 number request 33

Como se puede ver, el numero de requests no va alineado con el numero de tickets que se ha guardado en bd.