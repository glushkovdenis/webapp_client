import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Client {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int PORT = 7676;
    private static final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private static final SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
    private final static Scanner s = new Scanner(System.in);

    public Client(){
        try (
                Socket socket = new Socket(SERVER_ADDRESS, PORT);
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream())
        ) {
            mainMenu(input, output);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void mainMenu(DataInputStream input, DataOutputStream output) throws IOException {
        while (true) {
            System.out.print("\n" + "Найти(1), добавить(2), удалить(3) данные или выйти(4)? ");
            int n = s.nextInt();
            switch (n) {
                case(1):
                    searchData(input, output);
                    break;
                case(2):
                    howToInputData(input, output);
                    break;
                case(3):
                    deleteUser(input, output);
                    break;
                case(4):
                    return;
            }
        }
    }

    public void searchData(DataInputStream input, DataOutputStream output) throws IOException {
        System.out.print("Введите фамилию с заглавной буквы: ");
        String substring = s.next();

        output.writeInt(1);
        output.writeUTF(substring);

        String sInput = input.readUTF();
        if(sInput == null || sInput.isEmpty()) {
            System.out.println("Ошибка на сервере. Попробуйте позже!");
        } else {
            ArrayList<User> res = catchDamagedData(sInput);
            for(User u : res) {
                System.out.println(printData(u));
            }
        }
    }

    public ArrayList<User> catchDamagedData(String res) {
        ArrayList<JsonNode> jsonArray;
        try {
            jsonArray = new ArrayList<>(Arrays.asList(mapper.readValue(res, JsonNode[].class)));
        } catch (Exception e) {
            System.out.println("База данных повреждена.");
            return null;
        }
        if(jsonArray.isEmpty()){System.out.println("Запрашваемые данные не найдены.");}
        ArrayList<User> userArray = new ArrayList<>();

        for(JsonNode o: jsonArray) {
            try {
                User user = mapper.readValue(o.toString(), User.class);
                df.parse(user.getBirth());
                userArray.add(user);
            } catch (Exception e) {
                System.out.println("Невозможно вывести информацию, т.к. данные повреждены.");
            }
        }
        return userArray;
    }

    public String printData(User user) {
        return " \n" +
                "id: " + user.getId() + "\n" +
                "Name: " + user.getName() + "\n" +
                "Surname: " + user.getSurname() + "\n" +
                "Patronymic: " + user.getPatronymic() + "\n" +
                "Birth date: " + user.getBirth();
    }

    public void howToInputData(DataInputStream input, DataOutputStream output) throws IOException {
        User user;
        user = manualInput();
        System.out.println(printData(user));

        output.writeInt(2);
        output.writeUTF(mapper.writeValueAsString(user));
        System.out.println(input.readUTF());
    }

    public User manualInput() {
        User user = new User();

        System.out.print("Введите имя: ");
        user.setName(s.next());
        System.out.print("Введите фамилию: ");
        user.setSurname(s.next());
        System.out.print("Введите отчество: ");
        user.setPatronymic(s.next());
        user.setBirth(birthInput());
        user.setId(0);

        return user;
    }

    public String birthInput () {
        boolean checker = false;
        String date = "";
        while(!checker){
            System.out.print("Введите дату рождения: ");
            String input = s.next();
            df.setLenient(false);
            checker = birthCheck(false, df, input);
            date = input;
        }
        return date;
    }

    public boolean birthCheck(boolean checker, SimpleDateFormat df, String input) {
        while(!checker) {
            try {
                df.parse(input);
                checker = true;
            } catch (Exception e) {
                System.out.println("Неверный формат даты. Попробуйте ещё!");
                break;
            }
        }
        return checker;
    }

    public void deleteUser(DataInputStream input, DataOutputStream output) throws IOException {
        output.writeInt(3);
        String array = input.readUTF();
        ArrayList<User> userArray = new ArrayList<>(Arrays.asList(mapper.readValue(array, User[].class)));
        for(User u: userArray) {
            System.out.println(printData(u));
        }

        while (true) {
            System.out.print("Введите id: ");
            int id = s.nextInt();
            if(userArray.removeIf(user -> user.getId() == id)) {
                output.write(id);
                break;
            } else {
                System.out.println("Несуществующий id. Попробуйте ещё раз!");
            }
        }
        System.out.println("Обновлённый список данных: " + input.readUTF());
    }

}