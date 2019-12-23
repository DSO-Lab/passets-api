# Passets 被动资产识别引擎 API 接口模块

本模块以 REST API 的形式根据用户需求向用户返回所需数据。

## 运行环境

- JDK/JRE 1.8

## 容器构建

```
# X86_64
docker build -t passets-api:<tag> .
docker-compose build

# ARMv7
docker build -f Dockerfile_armv7 -t passets-api:<tag> .
```

## 容器启动

### 使用 docker 启动

#### 启动自行编译的本地镜像
```
docker run -it --rm -e ELASTICSEARCH_URL=<elasticsearch-host>:9200 -e ELASTICSEARCH_INDEX=logstash-passets -e SECRET=<api-secret> -p 8081:8080 passets-api:<tag>
或
docker-compose up -d
```

#### 启动 Docker HUB 上的镜像
```
docker run -it --rm -e ELASTICSEARCH_URL=<elasticsearch-host>:9200 -e ELASTICSEARCH_INDEX=passets-logstash -e SECRET=<api-secret> -p 8081:8080 dsolab/passets-api:<tag>
```

可自行修改 docker-compose.yml 中的 `services.api.image` 属性为 `dsolab/passets-api:<tag>`，然后执行下面的命令启动容器：
```
docker-compose up -d
```

###  使用 docker-compose 启动

docker-compose.yml
```
version: "3"

services:
  api:
    build:
      context: .
    image: passets-api:<tag>
    container_name: passets-api
    environment:
      - TZ=Asia/Shanghai
      - ELASTICSEARCH_URL=<elasticsearch-host>:9200    # ES地址
      - ELASTICSEARCH_INDEX=passets-syslog             # ES索引
      - SECRET=<api-secret>                            # API 接口访问密钥
    ports:
      - "8081:8080"                                    # API 服务端口
```
