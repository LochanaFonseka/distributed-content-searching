# Docker build

docker build -t distributed-content-search .

# Docker Run..... use correct port for the client node

docker run -p 3000:80 -e REACT_APP_API_BASE_URL=http://localhost:8081 distributed-content-search
