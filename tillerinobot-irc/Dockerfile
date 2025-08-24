# BUILD
FROM rust:1.89-alpine

WORKDIR /build/
ADD . .

RUN if [ -e target/release/main ]; then chmod u+x target/release/main; fi
RUN if ldd target/release/main; then echo "already built"; else apk add --no-cache musl-dev; cargo build --release; fi

# RUN
# curl image is based on alpine, so we're compatible
FROM curlimages/curl

COPY --from=0 /build/target/release/main .

CMD [ "./main" ]

EXPOSE 8080
