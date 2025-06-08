# Docker Hadoop Container

[![State-of-the-art Shitcode](https://img.shields.io/static/v1?label=State-of-the-art&message=Shitcode&color=7B5804)](https://github.com/trekhleb/state-of-the-art-shitcode)

目前hdfs-cluster的镜像体积是**3.22**GB，相比runoob教程的cluster_protocal镜像(6.2GB)，体积减少了**48.6%**。🚀

|    镜像    |优化前|优化后|节省空间|
|:----------:|:----|:----|:------|
|hdfs-cluster|6.2GB|3.22GB|2.98GB|

## 使用指南

### Flask Container

1. 将 `flask` 目录拉取到本地后，直接在 `flask` 目录内执行以下命令构建镜像：
    ```bash
    docker build -t flask .
    ```

2. 使用 `docker images` 查看镜像
    ```bash
    REPOSITORY      TAG       IMAGE ID       CREATED        SIZE
    flask           latest    5303cfbf82fb   35 hours ago   233MB
    ```

#### Flask 目录结构🗂️
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

1. HDFS 容器的搭建参考了 [菜鸟教程 Hadoop 教程](https://www.runoob.com/w3cnote/hadoop-tutorial.html)。由于教程中搭建的镜像体积高达 6.2GB，因此编写了一个 `Dockerfile` 集成全部依赖环境和步骤。减少存储空间占用的同时也更加方便部署。

2. 将 `hdfs_cluster` 目录拉取到本地。

3. 将Hadoop **3.3.6** 二进制文件解压并放置在 `hdfs_cluster` 目录下。

    官网下载地址：
    [Hadoop 3.3.6 下载地址](https://dlcdn.apache.org/hadoop/common/hadoop-3.3.6/hadoop-3.3.6.tar.gz)

4. 我已经在 `Dockerfile` 文件中直接将 Hadoop 二进制文件复制到 `/usr/local/hadoop`，以便简化操作并缩减镜像大小：
    ```Dockerfile
    COPY hadoop-3.3.6 /usr/local/hadoop
    ```

5. 执行以下命令构建镜像：
    ```bash
    docker build -t hdfs-cluster .
    ```

6. 使用 `docker images` 查看镜像是否构建成功：
    ```bash
    REPOSITORY      TAG       IMAGE ID       CREATED             SIZE
    hdfs-cluster    latest    3fd30855b0ac   About an hour ago   3.22GB
    ```

---

### Docker-Compose🖇️

用docker-compose可以批量管理容器集群。

编写一个`compose.yaml`，搭建falsk-hdfs环境。

1. 构建好flask镜像和hdfs-cluster镜像

2. 用docker-compose运行容器群
    ```bash
    docker-compose up
    ```

3. 用`docker ps -a`查看容器是否全部运行。

4. 进入nn容器

    ```bash
    docker exec -it nn su hadoop
    ```

5. 初始化hdfs配置
    
    ```bash
    hdfs namenode -format
    ```

6. 启动HDFS

    ```bash
    start-dfs.sh
    ```

7. 如果要更改HDFS配置文件，先停止HDFS

    ```bash
    stop-dfs.sh
    ```
    
### 测试连接

- 访问flask前端页面。

    ```bash
    curl http://localhost:5000
    ```

- 测试WebHDFS连接。

    ```bash
    curl http://localhost:5000/test-webhdfs
    ```

    预计返回`status: success`

- 测试RPC连接。

    ```
    curl http://localhost:5000/test-rpc
    ```

    预计返回`status: success`

- 测试DNS解析。

    ```
    curl http://localhost:5000/test-dns
    ```

    预计返回结果为namenode容器的IP地址`172.20.1.10`
