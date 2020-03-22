import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        System.out.println("Введите адрес конфига: ");
        String str = s.next();
        String[] arg = new String[1];
        arg[0] = str;
        new Client(arg);
    }
}