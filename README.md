# Some helper commands to list pods
## To Run
### Get all pods
```bash
./bb-kube.clj -v
```
### by Node
```bash
./bb-kube.clj -vn # List all Nodes
./bb-kube.clj -vn ip-123-123-123-123.aws-region-1.compute.internal # Filter by specific Node
```

### Compare feature branches
```bash
./bb-kube.clj -vr path/to/repo
```