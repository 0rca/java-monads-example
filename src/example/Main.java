package example;

import java.util.Scanner;
import java.util.function.Function;

interface Command<T> {

    T execute();

    default <U> Command<U> bind(Function<T,Command<U>> fn) {
        return () -> fn.apply(this.execute()).execute();
    }

    default <U>Command<U> then(Command<U> cmd) {
        return bind(t -> cmd);
    }
}

class GetLine implements Command<String> {
    Scanner scanner = new Scanner(System.in);

    @Override
    public String execute() {
        System.out.print("Side-effect: ");
        return scanner.nextLine();
    }
}

class PutStrLn implements Command {
    public PutStrLn(String string) {
        this.string = string;
    }
    private String string;

    @Override
    public Object execute() {
        System.out.print("Side-effect: ");
        System.out.println(string);
        return null;
    }
}

@SuppressWarnings("unchecked")
public class Main {
    static Command putStrLn(String string) {
        return new PutStrLn(string);
    }
    static Command<String> getLine() {
        return new GetLine();
    }
    static Command<String> ask(String prompt) {
        return putStrLn(prompt).then(getLine());
    }

    public static void main(String[] args) {
        Command io = ask("What is your name?").bind(name ->
                     ask("What is your year of birth?").bind(year -> {
                         Integer yearInt = Integer.parseInt(year);
                         return putStrLn(String.format("Hello, %s. You should be %d in %d", name, 2015 - yearInt, 2015));}));

        System.out.println("=== Side-effects beyond this point only ===");
        io.execute();
    }
}
