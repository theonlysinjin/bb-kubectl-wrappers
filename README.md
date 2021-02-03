# Some helper commands to list pods
## To Run
### Get all pods
```bash
./bb-podinfo.clj -v
```
### by Node
```bash
./bb-podinfo.clj -vn # List all Nodes
./bb-podinfo.clj -vn ip-123-123-123-123.aws-region-1.compute.internal # Filter by specific Node
```

### Compare feature branches
```bash
./bb-podinfo.clj -vr path/to/repo
```