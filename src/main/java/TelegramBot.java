import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class TelegramBot extends TelegramLongPollingBot {
    DbFunctions db = new DbFunctions();
    Connection conn;
    User user = null;
    String status = "";
    String category = "";

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            if (update.getMessage().hasText()) {
                long id = Long.parseLong(update.getMessage().getChatId().toString());
                String message = update.getMessage().getText();

//                System.out.println(status);

                if (message.equals("/start")) {
                    status = "start";
                    start(id, "Привет! \uD83D\uDC4B\uD83C\uDFFB \n\nЯ помогу тебе найти информацию о интересующем лекартве! \n\nАвторизируйся и получай ответ!");
                }

//                System.out.println(user);

                switch (status) {
                    case "start/login" -> startLoginCheck(id, message);
                    case "start/reg" -> startRegCheck(id, message);
                    case "main/search" -> searchDrug(id, message);
                    case "" -> start(id, "Привет! \uD83D\uDC4B\uD83C\uDFFB \n\nЯ помогу тебе найти информацию о интересующем лекартве! \n\nАвторизируйся и получай ответ!");
                    case "start" -> status = "start";
                    case "categories/add" -> categoriesAdd(id, message);
                    case "drugs/add" -> drugAdd(id, message);
                    default -> {
                        if (user != null && !user.isAdmin()) {
                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(id);
                            sendMessage.setText("Неизвестная команда.");
                            sendMessage(sendMessage);
                            SendPhoto sendPhoto = new SendPhoto();
                            sendPhoto.setChatId(id);
                            sendPhoto.setCaption("Ниже представлены функции, которые я выполняю: ");
                            sendKeyBoardMain(sendPhoto);
                        }
                        if (user==null) {
                            start(id, "Для использования бота необходимо войти в систему.\n\nВыберите соответствующее действие.");
                        }
                        if (user.isAdmin()) {
                            if (status.matches("^catEdit/[а-яА-Я\\s]+$")) {
                                String[] value = status.split("/");
                                categoryEditSave(id, value[1], message);
                            }
                            if (status.matches("^drugEdit/[а-яА-Я\\s]+$")) {
                                String[] value = status.split("/");
                                drugEditSave(id, value[1], message);
                            }
                        }
                    }
                }
            }
        } else if (update.hasCallbackQuery()) {
            String message = update.getCallbackQuery().getData();
            Long id = update.getCallbackQuery().getMessage().getChatId();

//            System.out.println("1   "+update.getCallbackQuery().getData());
            if (message.matches("^all/[а-яА-Я\\s]+$")) {
                showCategory(id, message);
            }
            if (message.matches("^drug/[а-яА-Я\\s/]+$") || message.matches("^favoritesShow/[а-яА-Я\\s]+$")) {
                showDrug(id, message);
            }
            if (message.matches("^favoritesAll/[а-яА-Я\\s]+$") || message.matches("^favoritesSearch/[а-яА-Я\\s]+$")) {
                addFavorites(id, message);
            }
            if (message.matches("^search/[а-яА-Я\\s]+$")) {
                showDrug(id, message);
            }
            if (message.matches("^delete/[а-яА-Я\\s]+$")) {
                deleteDrug(id, message);
            }
            if (message.matches("^catEditDel/[а-яА-Я\\s]+$")) {
                showCategoryAdmin(id, message);
            }
            if (message.matches("^drugsEditDel/[а-яА-Я\\s]+$")) {
                showCategory(id, message);
            }
            if (message.matches("^catEdit/[а-яА-Я\\s]+$")) {
                catEdit(id, message);
            }
            if (message.matches("^catDel/[а-яА-Я\\s]+$")) {
                catDel(id, message);
            }
            if (message.matches("^drugEdit/[а-яА-Я\\s]+$")) {
                drugEdit(id, message);
            }
            if (message.matches("^drugDel/[а-яА-Я\\s]+$")) {
                drugDel(id, message);
            }
            if (message.matches("^drgEditDel/[а-яА-Я\\s/]+$")) {
                showDrugAdmin(id, message);
            }

//            System.out.println("status ------------------------ " + status);
            switch (message) {

                case "start/login" -> startLogin(id);
                case "start/reg" -> startReg(id);

                case "main/all" -> mainAll(id);
                case "main/search" -> mainSearch(id);
                case "main/favorites" -> mainFavorites(id);
                case "main/exit" -> mainExit(id);

                case "main/admin" -> mainAdmin(id);
                case "admin/categories" -> adminChange(id, "categories");
                case "admin/drugs" -> adminChange(id, "drugs");

                case "categories/add" -> dataAdd(id, "categories");
                case "drugs/add" -> dataAdd(id, "drugs");

                case "categories/editDelete" -> dataEditDel(id, "categories");
                case "drugs/editDelete" -> dataEditDel(id, "drugs");

                case "back" -> {

                    String[] messages = status.split("/");
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(id);
                    SendPhoto sendPhoto = new SendPhoto();
                    sendPhoto.setChatId(id);

                    if (messages[0].equals("start")) {
                        start(id, "Привет! \uD83D\uDC4B\uD83C\uDFFB \n\nЯ помогу тебе найти информацию о интересующем лекартве! \n\nАвторизируйся и получай ответ!");
                    }
                    if (messages[0].equals("main")) {
                        sendPhoto.setCaption("Ниже представлены функции, которые я выполняю: ");
                        sendKeyBoardMain(sendPhoto);
                    }
                    if (messages[0].equals("all")) {
                        mainAll(id);
                        status = "main/all";
                    }
                    if (messages[0].equals("drug")) {
                        showCategory(id, status);
                        status = "all/" + messages[1];
                    }
                    if (messages[0].equals("favoritesAll")) {
                        showDrug(id, "drug/" + category + "/" + messages[1]);
                        status = "drug/" + category + "/" + messages[1];
                    }
                    if (messages[0].equals("favoritesShow")) {
                        mainFavorites(id);
                        status = "main/favorites";
                    }
                    if (messages[0].equals("search")) {
                        mainSearch(id);
                        status = "main/search";
                    }
                    if (messages[0].equals("favoritesSearch")) {
                        showDrug(id, "search/" + messages[1]);
                        status = "search/" + messages[1];
                    }
                    if (messages[0].equals("delete")) {
                        mainFavorites(id);
                        status = "main/favorites";
                    }
                    if (messages[0].equals("admin")) {
                        mainAdmin(id);
                        status = "main/admin";
                    }
                    if (messages[0].equals("drugs")) {
                        adminChange(id, "drugs");
                        status = "admin/drugs";
                    }
                    if (messages[0].equals("categories")) {
                        adminChange(id, "categories");
                        status = "admin/categories";
                    }
                    if (messages[0].equals("catEditDel")) {
                        dataEditDel(id, "categories");
                        status = "categories/editDelete";
                    }
                    if (messages[0].equals("catEdit")) {
                        showCategoryAdmin(id, status);
                        status = "catEditDel/"+messages[1];
                    }
                    if (messages[0].equals("catDel")) {
                        mainAdmin(id);
                        status = "main/admin";
                    }
                    if (messages[0].equals("drugsEditDel")) {
                        dataEditDel(id, "drugs");
                        status = "drugs/editDelete";
                    }
                    if (messages[0].equals("drgEditDel")) {
                        status = "drugsEditDel/"+category;
                        showCategory(id, status);
                    }
                    if (messages[0].equals("drugEdit")) {
                        status = "drgEditDel/" + category + "/" + messages[1];
                        showDrugAdmin(id, status);
                    }
                    if (messages[0].equals("drugDel")) {
                        status = "admin/drugs";
                        adminChange(id, "drugs");
                    }
                }
            }
            if (!update.getCallbackQuery().getData().equals("back")) {
                status = update.getCallbackQuery().getData();
            }
        }
    }


    public TelegramBot() {
        conn = db.connect_to_db("drugbd", "postgres", " ");
    }

    @Override
    public String getBotUsername() {
        return "drug_reference_bot";
    }

    @Override
    public String getBotToken() {
        return "6045373821:AAG4Y98EQWUY516hgddb2J_Cm-Oa_5Pq85s";
    }


    public void sendMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendPhoto(SendPhoto message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.out.println(e);
            throw new RuntimeException(e);
        }
    }

    private void start(Long id, String message) {
        SendPhoto sendMessage = new SendPhoto();
        sendMessage.setChatId(id);
        sendMessage.setCaption(message);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();

        List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Войти");
        button.setCallbackData("start/login");
        keyboardButtonsRow.add(button);
        rowList.add(keyboardButtonsRow);

        keyboardButtonsRow = new ArrayList<>();
        button = new InlineKeyboardButton();
        button.setText("Зарегистрироваться");
        button.setCallbackData("start/reg");
        keyboardButtonsRow.add(button);
        rowList.add(keyboardButtonsRow);

        inlineKeyboardMarkup.setKeyboard(rowList);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);

        InputFile photo = new InputFile(new File("C:\\Users\\днс\\IdeaProjects\\TelegramDrug\\src\\main\\java\\image\\hello.jpg"));
        sendMessage.setPhoto(photo);
        sendPhoto(sendMessage);
    }

    private void backForward(SendMessage message) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Назад");
        button.setCallbackData("back");
        keyboardButtonsRow.add(button);
        rowList.add(keyboardButtonsRow);
        inlineKeyboardMarkup.setKeyboard(rowList);

        message.setReplyMarkup(inlineKeyboardMarkup);
        sendMessage(message);
    }

    private void backForward(SendPhoto message) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Назад");
        button.setCallbackData("back");
        keyboardButtonsRow.add(button);
        rowList.add(keyboardButtonsRow);
        inlineKeyboardMarkup.setKeyboard(rowList);

        message.setReplyMarkup(inlineKeyboardMarkup);
        sendPhoto(message);
    }

    private void startLogin(long id) {
        SendPhoto message = new SendPhoto();
        message.setChatId(id);
        message.setCaption("Вход \n\nВведите через пробел свой логин и пароль. \n\nНапример: «login123 password123»");
        InputFile photo = new InputFile(new File("C:\\Users\\днс\\IdeaProjects\\TelegramDrug\\src\\main\\java\\image\\enter.jpg"));
        message.setPhoto(photo);
        backForward(message);
    }

    private void startLoginCheck(long id, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(id);
        String[] messages = message.split(" ");
        user = null;
        if (messages.length == 2) {
            if (db.readLogin(conn, messages[0], messages[1])) {
                SendPhoto message1 = new SendPhoto();
                message1.setChatId(id);
                user = new User(messages[0], messages[1]);
                user.setId(db.getId(conn, messages[0], messages[1]));
                message1.setCaption("Добро пожаловать " + messages[0] + "!\n\nНиже представлены функции, которые я выполняю: ");
                sendKeyBoardMain(message1);
            } else {
                sendMessage.setText("Ошибка! \n\nНеверный логин или пароль");
                backForward(sendMessage);
            }
        } else {
            sendMessage.setText("Ошибка! \n\nНеверный формат ввода");
            backForward(sendMessage);
        }
    }

    private void startReg(long id) {
        SendPhoto message = new SendPhoto();
        message.setChatId(id);
        message.setCaption("Регистрация \n\nВведите через пробел логин и пароль. \n\nПример: «login123 password123»");
        InputFile photo = new InputFile(new File("C:\\Users\\днс\\IdeaProjects\\TelegramDrug\\src\\main\\java\\image\\reg.jpg"));
        message.setPhoto(photo);
        backForward(message);
    }

    private void startRegCheck(long id, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(id);
        String[] messages = message.split(" ");

        if (messages.length == 2) {
            if (!db.readLogin(conn, messages[0], messages[1])) {
                db.insertUser(conn, messages[0], messages[1]);
                start(id, "Регистрация прошла успешно!");
            } else {
                sendMessage.setText("Ошибка! \n\nПользователь уже зарегистрирован");
                backForward(sendMessage);
            }
        } else {
            sendMessage.setText("Ошибка! \n\nНеверный формат ввода");
            backForward(sendMessage);
        }
    }

    private void sendKeyBoardMain(SendPhoto message) {
        status = "main";
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();

            switch (i) {
                case 0 -> {
                    button.setText("Каталог");
                    button.setCallbackData("main/all");
                    keyboardButtonsRow.add(button);
                    rowList.add(keyboardButtonsRow);
                }
                case 1 -> {
                    button.setText("Поиск");
                    button.setCallbackData("main/search");
                    keyboardButtonsRow.add(button);
                    rowList.add(keyboardButtonsRow);
                }
                case 2 -> {
                    if (!user.isAdmin()) {
                        button.setText("Избранное");
                        button.setCallbackData("main/favorites");
                        keyboardButtonsRow.add(button);
                        rowList.add(keyboardButtonsRow);
                    }
                }
                case 3 -> {
                    if (user.isAdmin()) {
                        button.setText("Управление данными");
                        button.setCallbackData("main/admin");
                        keyboardButtonsRow.add(button);
                        rowList.add(keyboardButtonsRow);
                    }
                }
                case 4 -> {
                    button.setText("Выход из системы");
                    button.setCallbackData("main/exit");
                    keyboardButtonsRow.add(button);
                    rowList.add(keyboardButtonsRow);
                }
            }
        }
        InputFile photo = new InputFile(new File("C:\\Users\\днс\\IdeaProjects\\TelegramDrug\\src\\main\\java\\image\\hello.jpg"));
        message.setPhoto(photo);
        inlineKeyboardMarkup.setKeyboard(rowList);
        message.setReplyMarkup(inlineKeyboardMarkup);
        sendPhoto(message);
    }

    private void mainAll(long id) {
        ArrayList<String> categories = db.readCategories(conn);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        for (String s : categories) {
            List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(s);
            button.setCallbackData("all/" + s);
            keyboardButtonsRow.add(button);
            rowList.add(keyboardButtonsRow);
        }
        SendPhoto message = new SendPhoto();
        message.setChatId(id);
        message.setCaption("Выберите раздел лекарств, который вас интересует: ");
        InputFile photo = new InputFile(new File("C:\\Users\\днс\\IdeaProjects\\TelegramDrug\\src\\main\\java\\image\\categories.jpg"));
        message.setPhoto(photo);
        backAny(message, rowList);
    }

    private void mainExit(long id) {
        user = null;
        status = "";
        start(id, "Вы вышли из системы!\n\n Для дальнейшего использования Бота, войдите снова");
    }

    private void backAny(SendMessage message, List<List<InlineKeyboardButton>> rowList) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Назад");
        button.setCallbackData("back");
        keyboardButtonsRow.add(button);
        rowList.add(keyboardButtonsRow);
        inlineKeyboardMarkup.setKeyboard(rowList);

        message.setReplyMarkup(inlineKeyboardMarkup);
        sendMessage(message);
    }

    private void backAny(SendPhoto message, List<List<InlineKeyboardButton>> rowList) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Назад");
        button.setCallbackData("back");
        keyboardButtonsRow.add(button);
        rowList.add(keyboardButtonsRow);
        inlineKeyboardMarkup.setKeyboard(rowList);

        message.setReplyMarkup(inlineKeyboardMarkup);
        sendPhoto(message);
    }

    private void showCategory(long id, String category) {
        String[] categoryShow = category.split("/");
        ArrayList<String> drugs = db.readDrugs(conn, categoryShow[1]);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();

        for (int i = 0; i < drugs.size(); i++) {
            List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(drugs.get(i));

            if (categoryShow[0].equals("drugsEditDel")) {
                button.setCallbackData("drgEditDel/" + categoryShow[1] + "/" + drugs.get(i));
            } else {
                button.setCallbackData("drug/" + categoryShow[1] + "/" + drugs.get(i));
            }

            keyboardButtonsRow.add(button);
            rowList.add(keyboardButtonsRow);
        }
        SendPhoto message = new SendPhoto();
        message.setChatId(id);
        message.setCaption("Список всех лекарств из категории «" + categoryShow[1] + "»:\n\n");
        InputFile photo = new InputFile(new File("C:\\Users\\днс\\IdeaProjects\\TelegramDrug\\src\\main\\java\\image\\drugs.jpg"));
        message.setPhoto(photo);
        backAny(message, rowList);
    }

    private void showDrug(long id, String category) {
        String[] drugsShow = category.split("/");
        ArrayList drugs;
        StringBuilder text = new StringBuilder();
        if (drugsShow[0].equals("drug")) {
            drugs = db.readDrug(conn, drugsShow[2]);
            this.category = drugsShow[1];
            for (int i = 0; i < drugs.size()-1; i++) {
                text.append(drugs.get(i)).append("\n\n");
            }
        } else {
            drugs = db.readDrug(conn, drugsShow[1]);
            for (int i = 0; i < drugs.size()-1; i++) {
                text.append(drugs.get(i)).append("\n\n");
            }
        }
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Назад");
        button.setCallbackData("back");
        keyboardButtonsRow.add(button);
        if (!user.isAdmin()) {
            if (drugsShow[0].equals("drug")) {
                InlineKeyboardButton button2 = new InlineKeyboardButton();
                button2.setText("Добавить в Избранное");
                button2.setCallbackData("favoritesAll/" + drugsShow[2]);
                keyboardButtonsRow.add(button2);
            }
            if (drugsShow[0].equals("search")) {
                InlineKeyboardButton button2 = new InlineKeyboardButton();
                button2.setText("Добавить в Избранное");
                button2.setCallbackData("favoritesSearch/" + drugsShow[1]);
                keyboardButtonsRow.add(button2);
            }
            if (drugsShow[0].equals("favoritesShow")) {
                InlineKeyboardButton button2 = new InlineKeyboardButton();
                button2.setText("Удалить из Избранного");
                button2.setCallbackData("delete/" + drugsShow[1]);
                keyboardButtonsRow.add(button2);
            }
        }

        if (drugs.get(drugs.size()-1)!=null) {
            SendPhoto message = new SendPhoto();
            message.setChatId(id);
            message.setCaption(String.valueOf(text));
            InputFile photo = new InputFile(new File((String) drugs.get(drugs.size() - 1)));
            message.setPhoto(photo);
            rowList.add(keyboardButtonsRow);
            inlineKeyboardMarkup.setKeyboard(rowList);
            message.setReplyMarkup(inlineKeyboardMarkup);
            sendPhoto(message);
        } else {
            SendMessage message = new SendMessage();
            message.setChatId(id);
            message.setText(String.valueOf(text));
            rowList.add(keyboardButtonsRow);
            inlineKeyboardMarkup.setKeyboard(rowList);
            message.setReplyMarkup(inlineKeyboardMarkup);
            sendMessage(message);
        }
    }

    private void addFavorites(long id, String drug) {
        SendMessage message = new SendMessage();
        message.setChatId(id);

        String[] favorites = drug.split("/");
        if (db.addFavorites(conn, favorites[1])) {
            message.setText("Лекарство «" + favorites[1] + "» успешно добавлено в Избранное!");
        } else {
            message.setText("Лекарство «" + favorites[1] + "» уже находится в Избранном!");
        }
        backForward(message);
    }

    private void mainFavorites(long id) {
        SendPhoto message = new SendPhoto();
        message.setChatId(id);
        ArrayList<String> favorites = db.readFavorites(conn, user.getId());

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();

        if (favorites.isEmpty()) {
            message.setCaption("Избранные лекарства: \n\nСписок пока пуст :(");
        } else {
            message.setCaption("Избранные лекарства: \n\n");

            for (int i = 0; i < favorites.size(); i++) {
                List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(favorites.get(i));

                button.setCallbackData("favoritesShow/" + favorites.get(i));

                keyboardButtonsRow.add(button);
                rowList.add(keyboardButtonsRow);
            }
        }
        InputFile photo = new InputFile(new File("C:\\Users\\днс\\IdeaProjects\\TelegramDrug\\src\\main\\java\\image\\favorites.jpg"));
        message.setPhoto(photo);
        backAny(message, rowList);
    }


    private void mainSearch(long id) {
        SendPhoto message = new SendPhoto();
        message.setCaption("Введите название интересующего лекарства.\n\n" + "Например: «Кагоцел»");
        message.setChatId(id);
        InputFile photo = new InputFile(new File("C:\\Users\\днс\\IdeaProjects\\TelegramDrug\\src\\main\\java\\image\\search.jpg"));
        message.setPhoto(photo);
        backForward(message);
    }



    private void searchDrug(long id, String message) {
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        ArrayList<String> drugs = db.searchDrug(conn, message);

        SendPhoto message1 = new SendPhoto();
        message1.setChatId(id);

        if (drugs.isEmpty()) {
            message1.setCaption("К сожалению, по вашему запросу ничего не найдено.");
            sendPhoto(message1);
            mainSearch(id);
        } else {
            message1.setCaption("Результаты поиска по запросу «" + message + "» :\n\n");
            for (int i = 0; i < drugs.size(); i++) {
                List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(drugs.get(i));
                button.setCallbackData("search/" + drugs.get(i));
                keyboardButtonsRow.add(button);
                rowList.add(keyboardButtonsRow);
            }
            InputFile photo = new InputFile(new File("C:\\Users\\днс\\IdeaProjects\\TelegramDrug\\src\\main\\java\\image\\res.jpg"));
            message1.setPhoto(photo);
            backAny(message1,rowList);
        }

    }

    private void deleteDrug(long id, String drug) {
        String[] drugName = drug.split("/");
        db.deleteDrug(conn, drugName[1]);

        SendMessage message = new SendMessage();
        message.setChatId(id);
        message.setText("Лекарство «" + drugName[1] + "» удалено из Избранного!");
        backForward(message);
    }


    private void mainAdmin(long id) {
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();

        List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Категории");
        button.setCallbackData("admin/categories");
        keyboardButtonsRow.add(button);
        rowList.add(keyboardButtonsRow);

        keyboardButtonsRow = new ArrayList<>();
        button = new InlineKeyboardButton();
        button.setText("Лекарства");
        button.setCallbackData("admin/drugs");
        keyboardButtonsRow.add(button);
        rowList.add(keyboardButtonsRow);

        SendPhoto message = new SendPhoto();
        message.setChatId(id);
        message.setCaption("Выберите данные для работы: ");
        InputFile photo = new InputFile(new File("C:\\Users\\днс\\IdeaProjects\\TelegramDrug\\src\\main\\java\\image\\data.jpg"));
        message.setPhoto(photo);
        backAny(message, rowList);
    }

    public void adminChange(Long id, String change) {
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();

        List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        if (change.equals("categories")) {
            button.setText("Добавить новую категорию");
            button.setCallbackData("categories/add");
        } else {
            button.setText("Добавить новое лекарство");
            button.setCallbackData("drugs/add");
        }
        keyboardButtonsRow.add(button);
        rowList.add(keyboardButtonsRow);

        keyboardButtonsRow = new ArrayList<>();
        button = new InlineKeyboardButton();
        if (change.equals("categories")) {
            button.setText("Изменить / Удалить категорию");
            button.setCallbackData("categories/editDelete");
        } else {
            button.setText("Изменить / Удалить лекарство");
            button.setCallbackData("drugs/editDelete");
        }
        keyboardButtonsRow.add(button);
        rowList.add(keyboardButtonsRow);


        SendMessage message = new SendMessage();
        message.setChatId(id);
        message.setText("Выберите действие которое Вас интересует: ");
        backAny(message, rowList);
    }

    public void dataAdd(Long id, String change) {
        SendMessage message = new SendMessage();
        message.setChatId(id);
        if (change.equals("categories")) {
            message.setText("Добавление новой категории. \n\nВведите название новой категории \n\nНапример: «ОРВИ»");
        } else {
            String text = "Добавление нового лекарства. \n\nВведите через «/» следующие характеристики лекарства: \n\n";
            text = text + "1. Название лекарства\n" +
                    "2. Описание лекарства\n" +
                    "3. Противопоказания\n" +
                    "4. Побочные эффекты\n" +
                    "5. Инструкция по применению\n" +
                    "6. Форма выпуска\n" +
                    "7. Срок годности\n" +
                    "8. Условия хранения\n" +
                    "9. Расположение изображения\n" +
                    "10. Категория (id) \n\n";
            message.setText(text);
//            message.setText("Например: «Кагоцел/Кагоцел - это иммуностимулирующее средство./Беременность, лактация./Редко возникают реакции аллергической природы./Принимать после еды./Таблетки по 12 мг и 50 мг./3 года./Хранить в сухом месте при температуре не выше 25 °C.»");
        }
        backForward(message);
    }

    public void categoriesAdd(Long id, String category) {
        SendMessage message = new SendMessage();
        message.setChatId(id);
        if (db.addCategory(conn, category)) {
            message.setText("Категория «" + category + "» успешно добавлена в каталог!");
        } else {
            message.setText("Категория «" + category + "» уже существует!");
        }
        backForward(message);
    }

    public void drugAdd(Long id, String drug) {
        SendMessage message = new SendMessage();
        message.setChatId(id);

        String[] drugData = drug.split("/");
        if (drugData.length == 10) {
            if (db.addDrug(conn, drugData)) {
                message.setText("Лекартсво «" + drug + "» успешно добавлена в каталог!");
            } else {
                message.setText("Лекартсво «" + drug + "» уже существует!");
            }
        } else {
            message.setText("Ошибка! Неверный формат ввода");
        }
        backForward(message);
    }

    public void dataEditDel(Long id, String change) {
        SendMessage message = new SendMessage();
        message.setChatId(id);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();

        if (change.equals("categories")) {
            message.setText("Выберите категорию:\n\n");
        } else if (change.equals("drugs")) {
            message.setText("Выберите категорию в которой находится интересующее Вас лекарство:\n\n");
        }
        ArrayList<String> categories = db.readCategories(conn);
        for (int i = 0; i < categories.size(); i++) {
            List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(categories.get(i));
            if (change.equals("categories")) {
                button.setCallbackData("catEditDel/" + categories.get(i));
            } else if (change.equals("drugs")) {
                button.setCallbackData("drugsEditDel/" + categories.get(i));
            }
            keyboardButtonsRow.add(button);
            rowList.add(keyboardButtonsRow);
        }
        backAny(message, rowList);
    }




    private void showCategoryAdmin(long id, String category) {
        String[] categoryName = category.split("/");
        SendMessage message = new SendMessage();
        message.setChatId(id);
        message.setText("Название Категории «"+categoryName[1]+"»\n\n");
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();

        List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Удалить");
        button.setCallbackData("catDel/"+categoryName[1]);
        keyboardButtonsRow.add(button);

        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("Изменить");
        button2.setCallbackData("catEdit/"+categoryName[1]);
        keyboardButtonsRow.add(button2);
        rowList.add(keyboardButtonsRow);

        backAny(message, rowList);
    }

    private void catEdit(long id, String category) {
        SendMessage message = new SendMessage();
        message.setChatId(id);
        String[] categoryName = category.split("/");
        message.setText("Название Категории «"+categoryName[1]+"».\n\n Введите новое название категории:\n\n");
        backForward(message);
    }


    private void categoryEditSave(long id, String oldValue, String newValue) {
        SendMessage message = new SendMessage();
        message.setChatId(id);
        if (db.categoryEdit(conn, oldValue, newValue)) {
            message.setText("Изменение сохранено.");
        } else {
            message.setText("Ошибка! Попробуйте позже.");
        }
        sendMessage(message);
        status = "main/admin";
        mainAdmin(id);
    }

    private void catDel(long id, String category) {
        SendMessage message = new SendMessage();
        message.setChatId(id);
        String[] categoryName = category.split("/");
        if (db.categoryDel(conn, categoryName[1])) {
            message.setText("Категория «" + categoryName[1] + "» удалена.\n\n");
        } else {
            message.setText("Ошибка! Попробуйте позже.\n\n");
        }
        backForward(message);
    }

    private void showDrugAdmin(long id, String category) {
        String[] drugsShow = category.split("/");
        ArrayList drugs = db.readDrug(conn, drugsShow[2]);
        this.category = drugsShow[1];
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < drugs.size()-1; i++) {
            text.append(drugs.get(i)).append("\n\n");
        }
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();

        List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Удалить");
        button.setCallbackData("drugDel/"+drugsShow[2]);
        keyboardButtonsRow.add(button);

        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("Изменить");
        button2.setCallbackData("drugEdit/"+drugsShow[2]);
        keyboardButtonsRow.add(button2);
        rowList.add(keyboardButtonsRow);

        if (!drugs.get(drugs.size()-1).equals("null")) {
            SendPhoto message = new SendPhoto();
            message.setChatId(id);
            message.setCaption(String.valueOf(text));
            InputFile photo = new InputFile(new File((String) drugs.get(drugs.size() - 1)));
            message.setPhoto(photo);
            backAny(message, rowList);
        } else {
            SendMessage message = new SendMessage();
            message.setChatId(id);
            message.setText(String.valueOf(text));
            backAny(message, rowList);
        }
    }

    private void drugDel(long id, String drug) {
        SendMessage message = new SendMessage();
        message.setChatId(id);
        String[] drugName = drug.split("/");
        if (db.drugDel(conn, drugName[1])) {
            message.setText("Лекарство «" + drugName[1] + "» удалено.\n\n");
        } else {
            message.setText("Ошибка! Попробуйте позже.\n\n");
        }
        backForward(message);
    }

    private void drugEdit(long id, String drug) {
        SendMessage message = new SendMessage();
        message.setChatId(id);
        String[] drugName = drug.split("/");
        message.setText("Название Лекарства «"+drugName[1]+"».\n\nВведите через «/» номер пункта, который хотите изменить, и новое значение: \n\n"+
        "1. Название лекарства\n" +
                "2. Описание лекарства\n" +
                "3. Противопоказания\n" +
                "4. Побочные эффекты\n" +
                "5. Инструкция по применению\n" +
                "6. Форма выпуска\n" +
                "7. Срок годности\n" +
                "8. Условия хранения\n" +
                "9. Расположение изображения\n" +
                "10. Категория (id) \n\nНапример: «1/Кагоцел»");
        backForward(message);
    }

    private void drugEditSave(long id, String oldValue, String newValue) {

        String[] dataDrug = newValue.split("/");
        SendMessage message = new SendMessage();
        message.setChatId(id);

            if (dataDrug.length==2 && dataDrug[0].matches("^\\d+$") && dataDrug[1].matches("[а-яА-Я\\s]+$")) {
                if (db.drugEdit(conn, oldValue, dataDrug[0], dataDrug[1])) {
                    message.setText("Изменение сохранено.");
                    sendMessage(message);
                    status = "drugEdit/"+dataDrug[1];
                    drugEdit(id, status);
                } else {
                    message.setText("Ошибка! Попробуйте позже.\n\n");
                    sendMessage(message);
                    status = "main/admin";
                    mainAdmin(id);
                }
            } else {
                message.setText("Ошибка! Неверный формат ввода.\n\n");
                sendMessage(message);
                status = "drugEdit/"+oldValue;
                drugEdit(id, status);
            }
    }

}
