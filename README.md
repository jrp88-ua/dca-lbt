# Detalles de ejecución
La práctica ha sido realizada utilizando [Java 21](https://jdk.java.net/21/), la última versión de Java, por esto es posible que no se tenga ya instalada.
Por esto, y para evitar problemas de tamaño de entrga en Moodle, se han prepadado en [GitHub](https://github.com/jrp88-ua/dca-lbt/releases/tag/v1.0.0) dos versiones ya compiladas del proyecto:
- `dca-lbt-jrp88.WINDOWS.PORTABLE.zip` contiene un zip que al extraerlo genera un fichero `.exe` junto con un icono y dos carpetas necesarias para que funcione el archivo `.exe`. Este ejecutable portable ha sido generado utilizndo `jpackage` de forma que se puede ejecutar incluso sin tener Java instalado.
- `lbt-lbt-jrp88.STANDALONE.JAR.jar` es un simple jar compilado pero se requiere de Java 21 para ser ejecutado, además de tener que ejecutarse desde consola.

# Bugzilla
Para probar bugzilla haremos uso de un conjunto de imágenes de docker preparadas con un composer para ejecutar bugzilla.
Las insctrucciones se encuentran [aquí](https://github.com/bugzilla/harmony/blob/main/docker/development.md).

Como podemos ver, en conjunto, todos los contenedores no hacen uso de demasiados recursos, no llenago ni a un 4% de uso de CPU ni 2GB de memoria, siendo siete contenedores en ejecucuión a la vez,
sin embargo notamos que estos hacen mucho uso de la red incluso cuando no hay usuarios usando la prágina web en el momento de tomar la captura de pantalla.

Tambien notamos que la aplicación es algo lenta para cargar pero esto puede que se deba a uso de contenedores.
![dockerhub.png](imgs/dockerhub-harmony.png)


En cuanto al uso, después de configurar el proxy de firefox como se indica en las instrucciones, probamos un poco la app
y notamos que es muy simple de usar, aunque la cantidad de botones y opciones en ciertas páginas, como la de búsqueda avanzada, 
es algo abrumante hasta que te acostumbras.
![bugzilla.png](imgs/bugzilla.png)

Creamos un issue de prueba

![crear-issue.png](imgs/crear-issue.png)

Y vemos que podemos comentar en el

![ver-issue.png](imgs/ver-issue.png)