- Generate a shared-ssh-key on your host.

``` Bash
    ssh-keygen -t rsa -P '' -f ssh_keys/id_rsa
```

- Create a authorize file.

``` Bash
    touch ssh_keys/authorized_keys
```

- Put the shared-ssh-keys into authorized file

``` Bash
    cat ssh_keys/id_rsa.pub >> ssh_keys/authorized_keys
```

By doing these can slove the problem that SSH-mutual-trust.