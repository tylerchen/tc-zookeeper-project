Zookeeper
=====

### 1.Usage for server

    curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/server/create" -d $'clientPort=2181\ndataDir=/Users/zhaochen/Desktop/temppath/zk'
    curl -k "https://localhost:8989/zk/server/list"
    curl -k "https://localhost:8989/zk/server/shutdown/0.0.0.0:2181"

### 2.Usage for client

    curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181'
    curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181 quit'
    curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181 redo 1'
    curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181 history'
    curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181 printwatches'
    curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181 connect host:port'
    curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181 create [-s] [-e] path data acl'
    curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181 delete path [version]'
    curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181 rmr path'
    curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181 set path data [version]'
    curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181 aget path'
    curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181 get path [watch]'
    curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181 ls path [watch]'
    curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181 ls2 path [watch]'
    curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181 getAcl path'
    curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181 setAcl path acl [aclVersion]'
    curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181 stat path [watch]'
    curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181 listquota path'
    curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181 setquota -n|-b val path'
    curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181 delquota [-n|-b] path'
    curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181 close'
    curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181 sync path'
    curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181 addauth scheme auth'
