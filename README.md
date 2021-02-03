## To Run

### Get all pods
./bb-kube.clj -v
### by Node
./bb-kube.clj -vn # List all Nodes
./bb-kube.clj -vn ip-123-123-123-123.aws-region-1.compute.internal # Filter by specific Node

### Compare feature branches
./bb-kube.clj -vr path/to/repo