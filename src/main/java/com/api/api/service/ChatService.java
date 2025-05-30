package com.api.api.service;

import com.api.api.DTO.ChatResponse;
import com.api.api.DTO.ChatResponseDTO.*;
import com.api.api.DTO.MessageDTO;
import com.api.api.constants.DailyFlags;
import com.api.api.constants.ErrorMessages;
import com.api.api.exceptions.AIResponseGenerationException;
import com.api.api.exceptions.NoContentException;
import com.api.api.exceptions.TodayChatAlreadyExists;
import com.api.api.model.Chat;
import com.api.api.model.DailyUserFlags;
import com.api.api.model.Message;
import com.api.api.model.User;
import com.api.api.repository.ChatRepository;
import com.api.api.repository.DailyUserFlagsRepository;
import com.api.api.repository.MessageRepository;
import com.api.api.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.MethodNotAllowedException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ChatService {

    private final ChatRepository chatRepository;

    private final UserRepository userRepository;

    private final MessageService messageService;

    private final MessageRepository messageRepository;

    private final DailyUserFlagsRepository dailyUserFlagsRepository;

    @Autowired
    public ChatService(ChatRepository chatRepository, UserRepository userRepository, MessageService messageService, MessageRepository messageRepository, DailyUserFlagsRepository dailyUserFlagsRepository) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
        this.messageService = messageService;
        this.messageRepository = messageRepository;
        this.dailyUserFlagsRepository = dailyUserFlagsRepository;
    }

    //Función para crear un nuevo chat
    public Chat createChat(User user) {
        //Al ser una función que llama addMessageToChat, no es necesario comprobar si el user ya existe pq donde llamamos a la función ya hemos hecho la comprobación
        Chat chat = new Chat();
        chat.setUser(user);
        return chat; //Devolvemos el chat a la función que lo ha llamado, pero aún no lo guardamos en la BD ya que tenemos que crearle un título
    }

    @Transactional
    //Función para que el user pueda mandar un mensaje a un chat existente o crear uno nuevo con el primer mensaje que ha enviado
    public ChatResponse addMessageToChat(Long idUser, Long idChat, Message message) {
        //tenemos que comprobar si el chat y el user existen
        String response = null;
        User user = this.userRepository.findById(idUser).orElseThrow(() -> new EntityNotFoundException(ErrorMessages.USER_NOT_FOUND));
        if (Objects.nonNull(idChat)) {
            //Comprobamos si el chat existe, si no existe lanzamos una excepción
            Chat chat = this.chatRepository.findById(idChat).orElseThrow(() -> new EntityNotFoundException("Chat no encontrado"));
            //Comprobamos que existe una relación entre el user y el chat
            boolean exits = this.chatRepository.existsByUserIdAndId(idUser, idChat);
            //Comprobamos si existe relación entre el chat y el user
            if (exits) {
                //Comprobamos si el chat es de hoy o no, si no es de hoy no se le puede mandar el mensaje
                if (isChatOfToday(idChat)) {
                    message.setChat(chat); //Añadimos el chat al mensaje
                    //Si existe la relación añadimos el mensaje al chat llamando a MessageService
                    response = this.messageService.sendMessage(message, chat.getId());
                    //Solo nos interesa devolver un ChatResponseDTO con el mensaje que la IA ha generado, usamos .trim() para quitar los espacios en blanco al principio y al final
                    if (response != null) return new IAResponseDTO(response.trim());
                    else throw new AIResponseGenerationException("No se ha podido enviar el mensaje");

                } else
                    throw new MethodNotAllowedException("addMessageToChat para chats que no son del día actual.", null);

            } else throw new AccessDeniedException("El usuario no tiene acceso a este chat");
        } else {
            //Antes de hacer toda la lógica tenemos que comprobar que el user no haya creado un chat en el día de hoy
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

            /*
             * Antes de empezar con la creación del chat tenemos que mirar si el user ha hecho un chat en el día de hoy y mirar si la bandera diaria que hace referencia
             * a si ha hecho un chat aunque lo haya eliminado después existe
             */
            boolean hasTodayChatFlag = this.dailyUserFlagsRepository.existsByUser_IdAndFlagKeyAndTimeStampBetween(idUser, DailyFlags.HAS_CHAT_TODAY, startOfDay, endOfDay);
            boolean hasTodayChat = this.chatRepository.existsByUserIdAndDateBetween(idUser, startOfDay, endOfDay);

            if (!hasTodayChat && !hasTodayChatFlag) {
                Chat newChat = createChat(user);
                //Primero tenemos que crear un título para el chat en base al primer mensaje que se ha enviado
                String title = this.messageService.createTitle(message);
                //Si el título es distinto de null, añadimos la info al chat y lo guardamos y enviamos el mensaje
                if (Objects.nonNull(title)) {
                    newChat.setName(title.trim());
                    this.chatRepository.save(newChat); //Ahora si podemos guardar el chat en la BD
                    /*
                     * Una vez que se ha creado el chat de hoy, lo guardamos en la BD y procedemos a crear las variables diarias
                     * de CHAT_ID_TODAY y HAS_CHAT_TODAY relacionadas con el user
                     */
                    this.createChatFlags(user, DailyFlags.CHAT_ID_TODAY, String.valueOf(newChat.getId()), endOfDay);
                    this.createChatFlags(user, DailyFlags.HAS_CHAT_TODAY, "true", endOfDay);
                    //Una vez creado el chat añadimos el mensaje, y la respuesta que obtenemos de la IA en la tabla de mensajes de la BD
                    message.setChat(newChat);
                    response = this.messageService.sendMessage(message, newChat.getId());
                    //Nos interesa devolver en el objeto tanto la info del chat que se ha creado como el mensaje que nos ha devuelto la IA
                    if (response != null) return new ChatCreatedDTO(newChat, response.trim());
                    else throw new AIResponseGenerationException("No se ha podido enviar el mensaje");
                } else throw new AIResponseGenerationException("No se ha podido crear un título para el chat");
            } else throw new TodayChatAlreadyExists("El usuario ya ha creado el chat diario");
        }
    }

    //Función aux privada para crear las instancias de las banderas del user relacionadas con el chat e insertarlas en la BD
    private void createChatFlags(User user, String flagKey, String flagValue, LocalDateTime expiryTime) {
        DailyUserFlags flag = new DailyUserFlags();
        flag.setFlagKey(flagKey);
        flag.setFlagValue(flagValue);
        flag.setUser(user);
        flag.setExpiryTime(expiryTime);
        this.dailyUserFlagsRepository.save(flag);
    }

    /*
     * Funciones que se usan para la gestión de los chats de un user:
     * 1. Recupera el historial de los chats de un user (valor de filter = history)
     * 2. Recupera os chats que ha tenido el user en los últimos tres meses (filter = last3Months)
     * 3. Recupera los chats que hay een un rango de fechas que puede especificar el user (filter = range)
     *
     * Esto se lo indicamos a la función en base a un parámetro que le pasamos en la petición (filter)
     */
    @Transactional //Para que no de error al hacer la consulta
    public List<ChatResponse> getChats(Long idUser, String filter, LocalDate startDate, LocalDate endDate) {
        //Comprobamos si el user existe en la BD
        User user = this.userRepository.findById(idUser).orElseThrow(() -> new EntityNotFoundException(ErrorMessages.USER_NOT_FOUND));
        //Definimos la lista donde guardaremos los chats que se recuperen de la BD
        List<Chat> chatsOfUser;
        //Comprobamos el valor del parámetro filter y en base a eso recuperamos los chats de la BD
        switch (filter) {
            case "history":
                //Recuperamos todos los chats que tiene el user
                chatsOfUser = this.chatRepository.findByUserIdOrderByDateDesc(idUser);
                break;

            case "last3Months":
                //Recuperamos los chats que tiene el user en los últimos tres meses
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime startDateLast3Months = now.minusMonths(3).withHour(0).withMinute(0).withSecond(0).withNano(0);
                LocalDateTime endDateLast3Months = now.withHour(23).withMinute(59).withSecond(59).withNano(999999999);
                chatsOfUser = this.chatRepository.findByUser_IdAndDateBetween(idUser, startDateLast3Months, endDateLast3Months);
                break;

            case "range":
                //Comprobamos que el rango de fechas es correcto, si no lo es lanzamos una excepción
                if (Objects.isNull(startDate) || Objects.isNull(endDate))
                    throw new IllegalArgumentException("El rango de fechas no es correcto. Debe especificar tanto la fecha de inicio como la fecha de fin.");
                if (startDate.isAfter(endDate))
                    throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin.");
                //Recuperamos los chats que tiene el user en el rango de fechas que ha especificado, tenemos que poner la hora de inicio a 00:00:00 y la hora de fin a 23:59:59.999999999 para que nos devuelva todos los chats que hay en ese rango
                //Si no podríamos estar no estar recuperando chats que si que están en el rango que se ha especificado
                LocalDateTime startDateRange = startDate.atStartOfDay();
                LocalDateTime endDateRange = endDate.atTime(23, 59, 59, 999999999);
                chatsOfUser = this.chatRepository.findByUser_IdAndDateBetween(idUser, startDateRange, endDateRange);
                break;

            default:
                //No debería entrar aquí, pero por si acaso lanzamos una excepción
                throw new IllegalArgumentException("El valor del parámetro filter no es correcto. Los valores válidos son: history, last3Months o range.");
        }

        if (chatsOfUser.isEmpty()) throw new NoContentException("El usuario no tiene chats");
        else {
            List<ChatResponse> chatResponses = new ArrayList<>();
            //Si el filtro es history o range, devolvemos la lista de chats en el formato que conseguimos en el DTO de ChatResponseDTO
            if (Objects.equals(filter, "history") || Objects.equals(filter, "range")) {
                for (Chat chat : chatsOfUser) chatResponses.add(new ChatDetailsDTO(chat));
            } else {
                //Estamos en el caso de que el filtro sea last3Months, por lo que tenemos que devolver la lista de chats en el formato de ChatContributionDTO
                for (Chat chat : user.getChats()) chatResponses.add(new ChatContributionDTO(chat));
            }
            return chatResponses;
        }
    }

    //Función para eliminar uno o varios chats de un user
    @Transactional
    public List<ChatDeletedDTO> deleteChats(Long idUser, List<Long> idChats) {
        //Comprobamos si el user existe
        User user = this.userRepository.findById(idUser).orElseThrow(() -> new EntityNotFoundException(ErrorMessages.USER_NOT_FOUND));
        //Recuperamos la lista de chats que pertenecen al usuario y están en la lista de Ids
        List<Chat> chats = this.chatRepository.findByUserIdAndIdIn(idUser, idChats);
        if (chats.size() != idChats.size()) throw new AccessDeniedException("Uno o más chats no pertenecen al usuario");
        //Si no se ha disparado la excepción podemos eliminar los chats tanto de la entidad del user como de la BD
        user.getChats().removeAll(chats);
        this.userRepository.save(user);
        this.chatRepository.deleteAll(chats);
        List<ChatDeletedDTO> chatResponses = new ArrayList<>();
        for (Chat chat : chats) chatResponses.add(new ChatDeletedDTO(chat));
        return chatResponses;
    }

    //Función para recuperar la conversación de un chat
    @Transactional
    public ChatMessagesDTO getChat(Long idUser, Long idChat) {
        //Comprobamos si el user existe
        userRepository.findById(idUser).orElseThrow(() -> new EntityNotFoundException(ErrorMessages.USER_NOT_FOUND));
        //Comprobamos si el chat existe
        chatRepository.findById(idChat).orElseThrow(() -> new EntityNotFoundException("Chat no encontrado"));
        //Comprobamos si existe una relación entre el user y el chat
        if (chatRepository.existsByUserIdAndId(idUser, idChat)) {
            //Comprobamos si el chat es de hoy o no
            boolean isChatOfToday = isChatOfToday(idChat);
            //Recuperamos la lista de mensajes del chat llamando a la función del repositorio de mensajes que nos los devuelve ya de manera ordenada (ascendiente)
            List<Message> messages = messageRepository.findByChat_IdOrderByIdAsc(idChat);
            //Tenemos que pasar estos mensajes a su correspondiente DTO
            List<MessageDTO> messageDTOs = new ArrayList<>();
            for (Message message : messages) messageDTOs.add(new MessageDTO(message));
            //Dependiendo de si el chat es de hoy o no, tenemos que indicar de manera correcta el valor de la bandera isEditable del DTO que vamos a devolver
            if (isChatOfToday) return new ChatMessagesDTO(messageDTOs, true);
            else return new ChatMessagesDTO(messageDTOs);
        } else throw new IllegalArgumentException("El usuario no tiene acceso a este chat");
    }

    //Función privada para saber si el chat es de hoy o no
    private boolean isChatOfToday(Long chatId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

        return this.chatRepository.existsByIdAndDateBetween(chatId, startOfDay, endOfDay);
    }
}
