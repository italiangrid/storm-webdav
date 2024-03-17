podman stop storm-webdav; podman rm storm-webdav; podman run -d -p 8085:8085 -p 8443:8443 -p 8086:8086 --name storm-webdav localhost/storm-webdav:latest
