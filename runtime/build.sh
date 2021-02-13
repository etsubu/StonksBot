mv ../build/libs/*-all.jar ./app.jar
# Substitute aws access keys from env variables
docker build -t etsubu/stonksbot:latest --build-arg appname=stonksbuild --build-arg AWS_DEFAULT_REGION=eu-west-1 --build-arg AWS_ACCESS_KEY_ID="${AWS_ACCESS_KEY_ID}" --build-arg AWS_SECRET_ACCESS_KEY="${AWS_SECRET_ACCESS_KEY}"