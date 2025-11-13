package com.github.john_g1t.app.usecase;

public interface UseCase<Request, Response> {
    Response execute(Request request);
}
