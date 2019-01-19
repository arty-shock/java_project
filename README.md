### How to run jar:

java -jar server.jar

java -jar client.jar

### How to create jar from sources:

Из корневого каталога:

mkdir build\server

javac -sourcepath Network/src/;Server/src/ Server/src/ru/chat/server/chatServer.java -d build\server

cd build\server

jar cfe server.jar ru.chat.server.chatServer *

cd ..\\..

mkdir build\client

javac -sourcepath Network/src/;Client/src/ Client/src/ru/chat/client/ClientWindow.java -d build\client

cd build\client

jar cfe client.jar ru.chat.client.ClientWindow *

### How to create jar from sources:

Из корневого каталога:

javadoc -sourcepath Network/src/;Client/src/ -subpackages ru -d javadoc\client

javadoc -sourcepath Network/src/;Server/src/ -subpackages ru -d javadoc\server

### How to use client:

После запуска клиента можно поменять имя (верхнее поле ввода, автоматически ставится Guest####).

В нижнем поле ввода набирается сообщение, которое отправляется по нажатию на клавишу Enter.

В левом окне отображается список подключенных клиентов.

Для отправки файла необходимо нажать кнопку Add File в открывшемся диалоговом окне выбрать файл для отправки.

Файл появится в списке (окно справа от истории чата) и будет доступен для загрузки всем подключенным клиентам. 

Для загрузки файла необходимо дважды кликнуть по файлу в списке и файл будет загружен в папку /client, которая создается там же, где лежит client.jar.

Файл удаляется из списка после того, как сообщение об отправке файла будет удалено из истории чата (100 сообщений).

История сообщений подгружается при подключении клиента к чату.
