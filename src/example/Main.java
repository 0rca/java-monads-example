package example;

import java.util.Scanner;
import java.util.function.Function;
import java.util.function.Supplier;

// Command is a supplier with side-effects
abstract class Command<T> implements Supplier<T>{
    T execute() {
        return get();
    }

    @Override
    public T get() {
        return execute();
    }
}

class FluentCommand<T> extends Command<T> implements Supplier<T> {
    final private Supplier<T> supplier;

    private FluentCommand(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    static <T> FluentCommand<T> of(final Supplier<T> supplier) {
        return new FluentCommand<>(supplier);
    }

    <U> FluentCommand<U> mbind(final Function<T, FluentCommand<U>> fn) {return of(() -> fn.apply(execute()).get()); }

    <U> FluentCommand<U> then(FluentCommand<U> cmd) { return mbind(t -> cmd); }


    @Override
    public T get() {
        return supplier.get();
    }
}

class GetLine extends Command<String> {
    Scanner scanner = new Scanner(System.in);

    @Override
    public String execute() {
        System.out.print("Side-effect: ");
        return scanner.nextLine();
    }
}

class PutStrLn extends Command {
    public PutStrLn(String string) {
        this.string = string;
    }
    private String string;


    @Override
    Object execute() {
        System.out.print("Side-effect: ");
        System.out.println(string);
        return null;
    }
}

class ParseYear extends Command<Integer> {
    String string;

    ParseYear(String year) {
        this.string = year;
    }

    @Override
    public Integer get() {
        return Integer.parseInt(string);
    }
}

@SuppressWarnings("unchecked")
public class Main {
    static FluentCommand<String> ask(String prompt) {
        return FluentCommand.of(new PutStrLn(prompt))
                            .then(FluentCommand.of(new GetLine()));
    }

    public static void main(String[] args) {
        Command io = ask("What is your name?").mbind(name ->
                     ask("What is your birth year?").mbind(year ->
                     FluentCommand.of(new ParseYear(year)).mbind(yearInt ->
                     FluentCommand.of(new PutStrLn(String.format("Hello, %s. You should be %d in %d", name, 2015 - yearInt, 2015))))));

        System.out.println("=== Side-effects beyond this point only ===");
        io.execute();
    }
}
