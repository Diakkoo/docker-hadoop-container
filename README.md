# Docker Hadoop Container

[![State-of-the-art Shitcode](https://img.shields.io/static/v1?label=State-of-the-art&message=Shitcode&color=7B5804)](https://github.com/trekhleb/state-of-the-art-shitcode)

So far, the image's size of hdfs-cluster is **3.22GB**, it reduced by **48.6%** than cluster_protocal image from runoob turtorial.🚀

|    image    |before|after|reduced|
|:----------:|:----|:----|:------|
|hdfs-cluster|6.2GB|3.22GB|2.98GB|

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
├── templates/
│   └── index.html
├── requirements.txt
└── Dockerfile
```

---

### HDFS-Cluster Container

1. The HDFS container is references the [菜鸟教程 Hadoop 教程](https://www.runoob.com/w3cnote/hadoop-tutorial.html). The original image size is **6.2GB**. So I wrote a `Dockerfile` by myself to integrate all standard dependent environments and steps, for the purpose of minimizing and convenience. 
    
2. Pull the `hdfs_cluster` directory locally.

3. Extract and place the Hadoop **3.3.6** binary file in the `hdfs_cluster` directory.

    Official website download link：
    [Hadoop 3.3.6 下载地址](https://dlcdn.apache.org/hadoop/common/hadoop-3.3.6/hadoop-3.3.6.tar.gz)

4. In the `Dockfile`, I choose to copy the Hadoop binary file in the `/usr/local/hadoop`, which is image's directory, not the .tar.gz archive, with the purpose of simplizing steps and reducing storage usage.
    ```Dockerfile
    COPY hadoop-3.3.6 /usr/local/hadoop
    ```

5. Execute the following command to build up image.
    ```bash
    docker build -t hdfs-cluster .
    ```

6. Execute command `docker images` to check out the image.
    ```bash
    REPOSITORY      TAG       IMAGE ID       CREATED             SIZE
    hdfs-cluster    latest    3fd30855b0ac   About an hour ago   3.22GB
    ```

---

### Docker-Compose🖇️

Docker-compose enables centralized management of container cluster.

I wrote a `compose.yaml` to set up a hdfs environment.

1. Before using docker-compose, please be sure the image were built up successfully.

2. Start the container cluster by docker-compose command.
    ```bash
    docker-compose up
    ```

3. Execute command `docker ps -a` to check out if all containers are running.

4. Enter the `nn` container terminal by docker command.

    ```bash
    docker exec -it nn su hadoop
    ```

5. Initialize HDFS configuration.
    
    ```bash
    hdfs namenode -format
    ```

6. Start HDFS.

    ```bash
    start-dfs.sh
    ```

7. Stop HDFS before changing configuration

    ```bash
    stop-dfs.sh
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
