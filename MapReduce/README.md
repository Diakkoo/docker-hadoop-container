### 参数传递错误问题

当我用规范的命令通过调度自己编写的MapReduce代码执行任务时，会出现参数传递错误

```
[hadoop@nn ~]$ hadoop jar $HADOOP_HOME/FixWordCount-1.0-SNAPSHOT.jar top.example.FixedWordCount /home/hadoop/local_input/test.csv /output_12
=== check out the path ===
input path: top.example.FixedWordCount
output path: /home/hadoop/local_input/test.csv
counts: 3
need to values: input and output
```

系统误将本应该为`input path`的`/home/hadoop/local_input/test.csv`识别成了`output_path`

通过在编写MapReduce任务时添加命令参数检测来调整可以解决

**Hadoop版本:**

```
[hadoop@nn ~]$ java -version
openjdk version "1.8.0_412"
OpenJDK Runtime Environment (build 1.8.0_412-b08)
OpenJDK 64-Bit Server VM (build 25.412-b08, mixed mode)
```

**宿主机JAVA版本:**

```
PS D:\myMapReduce> java -version
java version "1.8.0_202"
Java(TM) SE Runtime Environment (build 1.8.0_202-b08)
Java HotSpot(TM) Client VM (build 25.202-b08, mixed mode)
```