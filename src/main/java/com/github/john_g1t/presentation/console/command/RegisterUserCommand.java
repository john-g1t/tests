package com.github.john_g1t.presentation.console.command;

import com.github.john_g1t.app.usecase.UseCase;
import com.github.john_g1t.app.usecase.user.CreateUserRequest;
import com.github.john_g1t.presentation.console.input.ConsoleInputReader;
import com.github.john_g1t.presentation.view.ConsoleView;

public class RegisterUserCommand extends BaseCommand {
    private final UseCase<CreateUserRequest, Integer> createUserUseCase;

    public RegisterUserCommand(
        ConsoleView view,
        ConsoleInputReader reader,
        UseCase<CreateUserRequest, Integer> createUserUseCase
    ) {
        super(view, reader);
        this.createUserUseCase = createUserUseCase;
    }

    @Override
    public void execute() {
        try {
            String email = reader.readLine("Enter email: ");
            String password = reader.readLine("Enter password: ");
            String firstName = reader.readLine("Enter first name: ");
            String lastName = reader.readLine("Enter last name: ");

            var request = new CreateUserRequest(email, password, firstName, lastName);
            Integer userId = createUserUseCase.execute(request);

            view.showSuccess("User registered successfully with ID: " + userId);
        } catch (Exception e) {
            handleError("Registration failed", e);
        }
    }
}
