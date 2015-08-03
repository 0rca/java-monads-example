package example;

import java.util.Scanner;
import java.util.function.Function;

interface Command<T> {

    // execute :: Command t -> t
    T execute();

    // return :: t -> Command t
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

public class Main {
    static Function<String, Command<Void>>   putStrLn = s -> new PutStrLn(s);
    static Function<String, Command<String>> ask      = s -> putStrLn.apply(s).then(new GetLine());

    public static void main(String[] args) {
        Command<Integer> io =   ask.apply("What is your name?").bind(name ->
                                ask.apply("What is your year of birth?").bind(year ->
                                Command.pure(2015 - Integer.parseInt(year)).bind(age ->
                                Command.pure("Hello, " + name + ". You should be " + age + " in 2015").bind(ans ->
                                putStrLn.apply(ans).then(
                                Command.pure(age))))));

        System.out.println("=== Side-effects beyond this point only ===");
        Integer age = io.execute();
        System.out.print("Returned age: ");
        System.out.print(age);
    }
}
