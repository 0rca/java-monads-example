package example;

import java.util.Scanner;
import java.util.function.Function;

interface Command<T> {

    // execute :: Command t -> t
    T execute();

    // pure :: t -> Command t
    static <U> Command<U> pure(U value) {
        return () -> value;
    }

    // (>>=) :: Command t -> (t -> Command u) -> Command u
    default <U> Command<U> bind(Function<T,Command<U>> fn) {
        return () -> fn.apply(this.execute()).execute();
    }

    // (>>) :: Command t -> Command u -> Command u
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

class PutStrLn implements Command<Void> {
    public PutStrLn(String string) {
        this.string = string;
    }
    private String string;

    @Override
    public Void execute() {
        System.out.print("Side-effect: ");
        System.out.println(string);
        return null;
    }
}

//@SuppressWarnings("unchecked")
public class Main {
    static Command<Void> putStrLn(String string) {
        return new PutStrLn(string);
    }
    static Command<String> getLine() {
        return new GetLine();
    }
    static Command<String> ask(String prompt) {
        return putStrLn(prompt).then(getLine());
    }

    public static void main(String[] args) {
        Command<Integer> io = ask("What is your name?").bind(name ->
                     ask("What is your year of birth?").bind(year -> {
                         Integer yearInt = Integer.parseInt(year);
                         Integer age = 2015 - yearInt;
                         return putStrLn(String.format("Hello, %s. You should be %d in %d", name, age, 2015)).then(Command.pure(age));
                     }));

        System.out.println("=== Side-effects beyond this point only ===");
        Integer age = io.execute();
        System.out.print("Returned age: ");
        System.out.print(age);
    }
}
