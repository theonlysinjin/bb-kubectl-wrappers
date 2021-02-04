# Some helper commands to list pods
Requires the setup of kubectl and for commands like this to work,
```bash
kubectl get nodes
```
## To Run
### Get all pods
```bash
./bb-podinfo -v
```
### by Node
```bash
./bb-podinfo -vn # List all Nodes
./bb-podinfo -vn ip-123-123-123-123.aws-region-1.compute.internal # Filter by specific Node
```

### Compare feature branches
```bash
./bb-podinfo -vr path/to/repo
```
