import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.consolelibs.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.*;

//C:/Users/denis.glushkov/Desktop/config.json
public class Client {
    private static String IP = "";
    private static int PORT = 5182;
    private static final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private static final SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
    private final static Scanner s = new Scanner(System.in);
    private final static Pattern ipPattern = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
    private final static Pattern portPattern = Pattern.compile(
            "^([0-9]{1,4}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])$");

    public Client(String[] args){
        String CONFIG_PATH = argsToString(args);
        configs(CONFIG_PATH);
        try {
            CloseableHttpClient client = HttpClients.createDefault();

            boolean exit = mainMenu(client);

            if(exit) {
                client.close();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public String argsToString(String[] args) {
        StringBuilder sb = new StringBuilder();
        for(String s : args) {
            sb.append(s);
        }
        return sb.toString();
    }

    public void configs(String CONFIG_PATH) {
        Map<String, String> configs = new HashMap<>();
        while (true) {
            try {
                configs = mapper.readValue(
                        new File(CONFIG_PATH), new TypeReference<Map<String, String>>(){});
                if(validateConfigs(configs)) {
                    break;
                } else {
                    System.out.println("Данные файла повреждены. Будет создан новый файл.");
                    createConfigs(CONFIG_PATH);
                }
            } catch (Exception e) {
                System.out.println("Файл конфига повреждён или не найден. Задайте настройки вручную.");
                createConfigs(CONFIG_PATH);
            }
        }

        applyingConfigs(configs);

        System.out.println(IP);
        System.out.println(PORT);
    }

    public void createConfigs(String CONFIG_PATH) {
        String ip = validateIP();
        String port = validatePort();
        Map<String, String> map = new HashMap<>();
        map.put("PORT", port);
        map.put("IP", ip);
        try {
            FileWriter writer = new FileWriter(CONFIG_PATH);
            writer.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map));
            writer.close();
        } catch (IOException e) {
            e.getMessage();
        }
    }

    public String validateIP() {
        String ip = "";
        while(true) {
            System.out.print("Введите ip: ");
            ip = s.next();
            if(ipPattern.matcher(ip).matches()) {
                break;
            } else {
                System.out.println("Неверный формат IP-адресса. Попробуйте ещё!");
            }
        }
        return ip;
    }

    public String validatePort() {
        String port = "";
        while(true) {
            System.out.print("Введите port: ");
            port = s.next();
            if(portPattern.matcher(port).matches()) {
                break;
            } else {
                System.out.println("Неверный формат PORT. Попробуйте ещё!");
            }
        }
        return port;
    }

    public boolean validateConfigs(Map<String, String> configs) {
        boolean ok = false;
        boolean port = false;
        boolean ip = false;
        for(Map.Entry<String, String> e : configs.entrySet()) {
            if (e.getKey().equals("PORT")) {
                if(portPattern.matcher(e.getValue()).matches()) {
                    port = true;
                } else {
                    break;
                }
            } else if(e.getKey().equals("IP")) {
                if(ipPattern.matcher(e.getValue()).matches()) {
                    ip = true;
                }
            }
        }
        if (ip && port) {
            ok = true;
        }
        return ok;
    }

    public void applyingConfigs(Map<String, String> configs) {
        for(Map.Entry<String, String> e : configs.entrySet()) {
            if (e.getKey().equals("PORT")) {
                PORT = Integer.parseInt(e.getValue());
            } else if(e.getKey().equals("IP")) {
                IP = e.getValue();
            }
        }
    }

    public boolean mainMenu(CloseableHttpClient client) throws IOException {
        while(true) {
            try {
            System.out.print("\n" + "Найти(1), добавить(2), удалить(3) данные или выйти(4)? ");
            int n = s.nextInt();
            switch (n) {
                case (1):
                    searchData(client);
                    break;
                case (2):
                    inputData(client);
                    break;
                case (3):
                    deleteUser(client);
                    break;
                case (4):
                    return true;
                default:
                    System.out.println("\n" + "Команда не распознана. Повторте попытку!");
                }
            } catch (InputMismatchException e) {
                System.out.println("Команда не распознана. Повторте попытку!");
                s.next();
            }
        }
    }

    public void searchData(CloseableHttpClient client) throws IOException {
        System.out.print("Введите искомое значение: ");
        String substring = "?surname=" + new Scanner(System.in).next();

        HttpGet request = new HttpGet("http://localhost:5182/Servlet/" + substring);
        request.addHeader("accept", "text/html");
        HttpResponse response = client.execute(request);
        BufferedReader rd = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
        StringBuilder line = new StringBuilder();
        while (rd.ready()) {
            line.append("\n").append(rd.readLine());
        }
        System.out.println(line.toString());
    }

    public String printData(User user) {
        return " \n" +
                "id: " + user.getId() + "\n" +
                "Name: " + user.getName() + "\n" +
                "Surname: " + user.getSurname() + "\n" +
                "Patronymic: " + user.getPatronymic() + "\n" +
                "Birth date: " + user.getBirth();
    }

    public void inputData(CloseableHttpClient client) throws IOException {
        User user;
        user = manualInput();
        HttpPost post = new HttpPost("http://localhost:5182/Servlet/");
        post.setEntity(new StringEntity(mapper.writeValueAsString(user)));
        post.setHeader("Accept", "application/json");
        post.setHeader("Content-type", "application/json");

        HttpResponse response = client.execute(post);
        assert(response.getStatusLine().getStatusCode() == 200);
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

    public void deleteUser(CloseableHttpClient client) throws IOException {
        HttpGet request = new HttpGet("http://localhost:5182/Servlet/");
        request.addHeader("accept", "text/json");
        HttpResponse response = client.execute(request);
        BufferedReader rd = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
        StringBuilder line = new StringBuilder();
        while (rd.ready()) {
            line.append("\n").append(rd.readLine());
        }

        if(line.toString().equals("0")) {
            System.out.println("\n" + "Ошибка на сервере. Повторите попытку позже");
            return;
        } else if (line.toString().equals("00")) {
            System.out.println("\n" + "Записей в базе данных не обнаружено.");
            return;
        }
        ArrayList<User> userArray = new ArrayList<>(Arrays.asList(mapper.readValue(line.toString(), User[].class)));
        for(User u: userArray) {
            System.out.println(printData(u));
        }

        int id = 0;
        while (true) {
            while(true) {
                System.out.print("Введите id: ");
                try {
                    id = s.nextInt();
                    break;
                } catch (Exception e) {
                    System.out.println("Неверный формат ввода. Попробуйте снова.");
                    s.next();
                }
            }
            final int finalId = id;
            if(userArray.removeIf(user -> user.getId() == finalId)) {
                break;
            } else {
                System.out.println("Несуществующий id. Попробуйте ещё раз!");
            }
        }

        HttpDelete delete = new HttpDelete("http://localhost:5182/Servlet/?id=" + id);
        HttpResponse deleteResponse = client.execute(delete);
    }
}