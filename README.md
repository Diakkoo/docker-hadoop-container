# Docker Hadoop Container

[![State-of-the-art Shitcode](https://img.shields.io/static/v1?label=State-of-the-art&message=Shitcode&color=7B5804)](https://github.com/trekhleb/state-of-the-art-shitcode)

**Docker-Compose ---> Flask + Haoop + Hive + MySQL**



The image's size of hadoop-base is **3.22GB**, it reduced by **48.6%** than cluster_protocal image from runoob turtorial.🚀

|    Image    |Before|After|Reduced|
|:----------:|:----|:----|:------|
|hadoop-base|6.2GB|3.22GB|2.98GB|

## Resource Requirements

[Hadoop 3.3.6](https://dlcdn.apache.org/hadoop/common/hadoop-3.3.6/hadoop-3.3.6.tar.gz)

[Hive 3.1.2](https://dlcdn.apache.org/hive/hive-4.0.1/apache-hive-4.0.1-bin.tar.gz)

[MySQL-Connector-JAVA-5.1.49](https://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-java-5.1.49.tar.gz)

## Usage

### Flask Container

1. After pulling the `flask` directory locally, and then execute the following command in the  `flask` directory to build up image.
    ```bash
    docker build -t flask .
    ```

2. Execute `docker images` to check out the image.
    ```bash
    REPOSITORY      TAG       IMAGE ID       CREATED        SIZE
    flask           latest    5303cfbf82fb   35 hours ago   233MB
    ```

#### Structure of Flask Directory🗂️
```
flask/
    ├── app.py
    ├── conf.py
    ├── connection/
    |   ├── __init__.py
    |   └── conn_routes.py
    ├── download/
    |   ├── __init__.py
    |   └── dwl_routes.py
    ├── generate/
    |   ├── __init__.py
    |   └── gnr_routes.py
    ├── upload/
    |   ├── __init__.py
    |   └── upl_routes.py
    ├── templates/
    │   └── index.html
    ├── requirements.txt
    └── Dockerfile
```

---

### Hadoop-Base Container as Datanode

The HDFS container is references the [菜鸟教程 Hadoop 教程](https://www.runoob.com/w3cnote/hadoop-tutorial.html). The original image size is **6.2GB**. So I wrote a `Dockerfile` by myself to integrate all standard dependent environments and steps, for the purpose of minimizing and convenience. 

#### Structure of Hdfs_Datanode Directory🗂️

To build up image, the structure should include following resources: 

```
hdfs_datanode/
            ├── ssh_keys/
            |   ├── id_rsa
            |   ├── id_rsa.pub
            |   └── authorized_keys
            ├── hadoop-3.3.6/
            │   ├── bin/
            |   ├── etc/
            |   └── ......
            └── Dockerfile
```
    
1. Pull the `hdfs_datanode` directory locally.

2. Extract and place the Hadoop **3.3.6** binary file in the `hdfs_datanode` directory.

    Official website download link：
    [Hadoop 3.3.6 下载地址](https://dlcdn.apache.org/hadoop/common/hadoop-3.3.6/hadoop-3.3.6.tar.gz)

3. In the `Dockfile`, I choose to copy the Hadoop binary file in the `/usr/local/hadoop`, which is image's directory, not the .tar.gz archive, with the purpose of simplizing steps and reducing storage usage.
    ```Dockerfile
    COPY hadoop-3.3.6 /usr/local/hadoop
    ```

4. Execute the following command to build up image.
    ```bash
    docker build -t hadoop-base .
    ```

5. Execute command `docker images` to check out the image.
    ```bash
    REPOSITORY      TAG       IMAGE ID       CREATED             SIZE
    hadoop-base    latest    3fd30855b0ac   About an hour ago   3.22GB
    ```

### Hadoop-Hive Container as Namenode

#### Structure of Hdfs_Datanode Directory🗂️

To build up image, the structure should include following resources: 

```
hdfs_namenode/
            ├── ssh_keys/
            |   ├── id_rsa
            |   ├── id_rsa.pub
            |   └── authorized_keys
            ├── hadoop-3.3.6/
            │   ├── bin/
            |   ├── etc/
            |   └── ......
            ├── apache-hive-3.1.2-bin/
            │   ├── bin/
            |   ├── conf/
            |   └── ......
            ├── mysql-connector-java-5.1.49
            │   ├── mysql-connector-java-5.1.49.jar
            |   └── ......
            ├── hive-site.xml
            └── Dockerfile
```

Execute the following command to build up image.

```bash
docker build -t hadoop-base .
```

---

### Docker-Compose🖇️

Docker-compose enables centralized management of container cluster.

I wrote a `compose.yaml` to set up a hdfs environment.

- Before using docker-compose, please be sure the image were built up successfully.

- Start the container cluster by docker-compose command.
    ```bash
    docker-compose up -d
    ```

- Execute command `docker ps -a` to check out if all containers are running.

#### Start HDFS

1. Enter the `nn` container terminal by docker command.

    ```bash
    docker exec -it nn su hadoop
    ```

2. Initialize HDFS configuration.
    
    ```bash
    hdfs namenode -format
    ```

3. Start HDFS.

    ```bash
    start-dfs.sh
    ```

    Stop HDFS before changing configuration

    ```bash
    stop-dfs.sh
    ```

#### Start Hive

1. Initialize Hive configuration in `$HIVE_HOME/bin`.

    ```
    schematool -dbType mysql -initSchema
    ```
2. Start Metastore.

    ```
    nohup hive --service metastore &
    ```

    Stop Metastore.

    ```
    pkill -f "hive --service metastore"
    ```

3. Start Hiveserver2.

    ```
    nohup hive --service hiveserver2 &
    ```

    Stop Hiveserver2.

    ```
    pkill -f "hive --service hiveserver2"
    ```

4. Enter Hive JDBC(Beeline) CLI

    ```
    beeline -u "jdbc:hive2://localhost:10000/default" -n hadoop
    ```
    
### Connection Tests

- Access flask fronted.

    ```bash
    curl http://localhost:5000
    ```

- Test WebHDFS connectivity.

    ```bash
    curl http://localhost:5000/test-webhdfs
    ```

    Expected respone: `status: success`

- Test RPC connectivity.

    ```
    curl http://localhost:5000/test-rpc
    ```

    Expected respone: `status: success`

- Test DNS resolution.

    ```
    curl http://localhost:5000/test-dns
    ```

    Excepted respone: NameNode container IP address(e.g. `172.20.1.10`)
