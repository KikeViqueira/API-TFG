package com.api.api.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.api.api.DTO.DrmObjectDTO;
import com.api.api.DTO.OnboardingAnswerDTO;
import com.api.api.DTO.FormRequestDTO.DRMRequestDTO;
import com.api.api.model.SleepLogAnswer;
import com.api.api.model.User;

@Service
public class GeminiService {

    private final WebClient webClient;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Autowired
    public GeminiService(WebClient webClient){
        this.webClient = webClient;
    }

    //Creamos la función para mandar un mensaje a la api de gemini como prompt
    public String sendMessage(String message){ 
        String apiUrl = "/models/gemini-1.5-flash:generateContent?key=" + apiKey; // Cambiar el modelo a 1.5 Flash

        System.out.println("\n\nMensaje que estamos mandado a la IA: "+ message + "\n\n");


        //PROMPT PARA INDICARLE EL FORMATO DE LA RESPUESTA A LOS MENSAJES DEL USER
        String prompt = "Responde a lo que te dice el user siempre basándote en teorías de la psicología de manera clara (si lo que te ha dicho tiene que ver con sus sueños o hábitos), en el resto de casos contesta brevemente que no estas diseñado para responder temas distintos. Quiero que respondas al user de manera profesional pero cercana y respuestas medias/cortas como si fuese una conversación de Whatsapp pero en detalle pero yendo al grano y sin expandirte demasiado pero sin ser cortante.";

        String fullPrompt = message + "\n\n" + prompt;


        //Construímos el cuerpo de la petición
        /*
        "contents": [{
        "parts":[{"text": "Explain how AI works"}]
        }]
    */
        Map<String, Object> requestBody = Map.of(
            "contents", List.of(Map.of(
                "parts", List.of(Map.of("text", fullPrompt))
            ))
        ); //Creamos un mapa con la estructura que espera la API

        //llamamos a la api de gemini
        return webClient.post().uri(apiUrl)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE) //Especificamos el tipo de contenido, en este caso JSON
        .bodyValue(requestBody)
        .retrieve()
        .bodyToMono(String.class) // Convertimos la respuesta en String (puede ajustarse según el JSON de Gemini)
        .block();
    }


    //FUNCION PARA CREAR UN TÍTULO EN BASE AL CONTENIDO DEL MENSAJE
    public String createTitle(String message){ 
        String apiUrl = "/models/gemini-1.5-flash:generateContent?key=" + apiKey; // Cambiar el modelo a 1.5 Flash

        //PROMPT PARA INDICARLE EL FORMATO DE LA RESPUESTA A LOS MENSAJES DEL USER
        String prompt = "Haz un titulo de un chat para el mensaje que te he enviado de maximo tres palabras, solo quiero que me devuelvas estrictamente esto, nada más.";

        String fullPrompt = message + "\n\n" + prompt;


        //Construímos el cuerpo de la petición
        Map<String, Object> requestBody = Map.of(
            "contents", List.of(Map.of(
                "parts", List.of(Map.of("text", fullPrompt))
            ))
        ); //Creamos un mapa con la estructura que espera la API

        //llamamos a la api de gemini
        return webClient.post().uri(apiUrl)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE) //Especificamos el tipo de contenido, en este caso JSON
        .bodyValue(requestBody)
        .retrieve()
        .bodyToMono(String.class) // Convertimos la respuesta en String (puede ajustarse según el JSON de Gemini)
        .block();
    }

    //Función para generar un informe sobre la roma de decisiones en base a la info del user
    public String generateReport(Map<String, Float> sleepLogsLastWeek,Map<Long, SleepLogAnswer> sleepLogsForContext, OnboardingAnswerDTO onboardingAnswerDTO, DRMRequestDTO drmRequestDTO, User user){
        String apiUrl = "/models/gemini-1.5-flash:generateContent?key=" + apiKey; // Cambiar el modelo a 1.5 Flash

        //PROMPT PARA INDICARLE EL FORMATO DEL INFORME DE LA TOMA DE DECISIONES Y QUE ES LO QUE TIENE QUE TENER EN CUENTA PARA HACERLO
        String prompt = """
        A continuación, se te entrega la siguiente información con el objetivo de generar un informe semanal profesional que analice cómo el sueño impacta en la calidad de la toma de decisiones del usuario. La información se compone de varios bloques:

        1. Registro de sueño de la última semana (sleepLogsLastWeek):
        - Es un mapa donde cada clave representa un día de la semana (por ejemplo, "Lunes", "Martes", etc.) y el valor asociado es el número de horas que el usuario durmió ese día.
        - Nota: Un valor de 0 indica que el usuario no registró las horas de sueño en ese día.

        2. Respuestas del Onboarding (OnboardingAnswerDTO):
        Este objeto contiene las respuestas a preguntas relacionadas con los hábitos de sueño y estilo de vida del usuario:
            - question1 ("¿Cuántas horas sueles dormir?"):
                    Opciones disponibles:
                    • "Menos de 5 horas"
                    • "Entre 5 y 6 horas"
                    • "Entre 6 y 7 horas"
                    • "Entre 7 y 8 horas"
                    • "Más de 8 horas"
            - question2 ("¿Cuántos días haces actividad física a la semana?"):
                    Opciones:
                    • "Ninguno"
                    • "Entre 1 y 2 días"
                    • "Entre 3 y 4 días"
                    • "Entre 5 y 6 días"
                    • "Todos los días"
            - question3:
                    Se espera un valor numérico (por ejemplo, "24") que representa la edad del usuario. Este dato se utiliza para determinar en qué rango de edad se encuentra el usuario y así personalizar el informe.
            - question4 ("¿Qué tipo de alimentación llevas?"):
                    Opciones:
                    • "Omnívora"
                    • "Vegetariana"
                    • "Vegana"
                    • "Flexitariana"
                    • "Otro"
            - question5 ("¿Cuál es tu nivel de estrés diario?"):
                    Opciones:
                    • "Muy bajo"
                    • "Bajo"
                    • "Moderado"
                    • "Alto"
                    • "Muy alto"

        3. Respuestas DRM (DRMRequestDTO - DrmAnswersUser):
        Este objeto recopila respuestas relacionadas con la toma de decisiones del usuario durante el día:
            - drm_question1 ("¿Cómo calificarías tu nivel de concentración durante el día?"):
                    Se espera un valor numérico en una escala del 1 al 10.
            - drm_question2 ("¿Percibiste que tu estado de ánimo y descanso influyeron en las decisiones que tomaste hoy?"):
                    Opciones disponibles: "Mucho", "Algo", "Poco", "Nada".
            - drm_question3 ("¿Qué tan satisfecho estás con las decisiones que tomaste hoy?"):
                    Se espera un valor numérico en una escala del 1 al 10.
            - drm_question4 ("¿Experimentaste momentos de estrés o presión al tomar decisiones?"):
                    Opciones: "Sí, con frecuencia", "A veces", "No, casi nunca".
            - drm_question5 ("¿Cuál fue la emoción que predominó en tu día?"):
                    Opciones: "Alegría", "Estrés", "Tristeza", "Neutral", "Otra".
            - drm_question6 ("Comentarios adicionales"):
                    Es un campo de texto opcional (máximo 255 caracteres) donde el usuario puede aportar comentarios adicionales.

        4. Registro completo de sueño para contexto (sleepLogsForContext):
        Este mapa ofrece detalles adicionales de los registros de sueño del usuario y contiene los siguientes campos en cada registro:
            - wakeuptime: La hora a la que el usuario se despertó.
            - sleepTime: La hora a la que el usuario se fue a dormir.
            - duration: La duración total del sueño.
            - answer1: Respuesta a la pregunta "¿Cómo ha sido la calidad de tu sueño?"
                        Posibles valores: "Muy buena", "Buena", "Regular", "Mala", "Muy mala".
            - answer2: Respuesta a la pregunta "¿Cómo te sientes de descansado?"
                        Posibles valores: "Muy descansado", "Descansado", "Ni descansado ni cansado", "Cansado", "Muy cansado".

        5. Recomendaciones de sueño (sleepRecommendations):
        Se proporcionan recomendaciones de sueño basadas en rangos de edad. Cada entrada incluye:
            - ageRange: El rango de edad al que aplica (por ejemplo, "6-13", "14-17", "18-25", "26-64", "65+").
            - hours: Valores fijos (plantilla) para cada rango de edad:
                    • Para "6-13":     minHours = 9,   idealHours = 10,  maxHours = 11.
                    • Para "14-17":    minHours = 8,   idealHours = 9,   maxHours = 10.
                    • Para "18-25":    minHours = 7,   idealHours = 8,   maxHours = 9.
                    • Para "26-64":    minHours = 7,   idealHours = 8,   maxHours = 9.
                    • Para "65+":      minHours = 7,   idealHours = 7.5, maxHours = 8.
            - phases: (Información sobre las fases del sueño que se registra, pero no es necesario analizarla ya que aún no está implementado.)

        Propósito del Informe:
        El objetivo es generar un breve informe semanal que analice de forma profesional cómo la duración y calidad del sueño, junto con otros factores (actividad física, alimentación, nivel de estrés y concentración), impactan en la calidad de la toma de decisiones del usuario. Se espera que el informe integre la información de los registros diarios, las respuestas del onboarding y las respuestas relacionadas con la toma de decisiones, proporcionando una visión integral del estado del sueño y su influencia en el desempeño diario.
        El informe tiene que tener tanto una buena argumentación como un buen análisis de la toma de decisiones del user en base a la información que has recibido pasado.
        Está bien que menciones algunas de las respuestas que el user ha puesto en los distintos cuestionarios pero haz la redacción del informe de manera profesional y cercana, como si fuese un informe de un profesional de la salud que se lo entrega a un paciente.
        Y que dicha redacción sea de una forma fluída.

        Resumen del objetivo para que lo puedas entender mejor:
        Utilizando estos datos, genera un informe semanal en texto plano, sin títulos, encabezados o secciones (pero si que puedes usar parrafos). El informe debe ser un análisis profesional y cercano sobre cómo la duración y calidad del sueño han impactado la toma de decisiones del usuario, indicando de manera clara y fundamentada qué aspectos podría estar haciendo mal o qué problemas podría estar presentando.
        Incluye ejemplos y valores específicos cuando sea relevante.
        QUIERO QUE HAGAS UNA CONCLUSIÓN COMPLETA Y PROFESIONAL DE COMO TODA LA INFO QUE TE DA EL USER IMPACTA EN LA TOMA DE DECISIONES, NO QUIERO QUE LE DES RECOMENDACIONES PARA MEJORAR LA SITUACIÓN PQ DE ESTO
        SE ENCARGARÁ OTRA PERSONA. Al empezar el informe si quieres te puedes referir al usario pero de una manera profesional.

        Además, se pasa la edad del usuario (en el valor ageUser) para determinar en qué rango de edad se encuentra, lo que permite personalizar el informe y hacerlo más profesional.
        
        Ya tienes la estructura para que entiendas la info que se te pasa del user, te la paso a continuación:
        sleepLogsLastWeek: %s
        onboardingAnswerDTO: %s
        drmRequestDTO: %s
        sleepLogsForContext: %s
        fecha de nacimiento: %s
        edad (Si vale -1 significa que la fech de nacimiento es null): %s
        userName: %s

        Realiza con toda esta información lo que se te ha pedido en los objetivos, que el informe sea de aproximadamente 15 líneas o un poco mas si es necesario ya que se va a desplegar este texto en un dispositivo móvil.
        """.formatted(sleepLogsLastWeek.toString(), onboardingAnswerDTO.toString(), drmRequestDTO.toString(), sleepLogsForContext.toString(), user.getBirthDate().toString(), String.valueOf(user.getAge()) , user.getName());
        

        System.out.println(prompt);

        //Construímos el cuerpo de la petición
        Map<String, Object> requestBody = Map.of(
            "contents", List.of(Map.of(
                "parts", List.of(Map.of("text", prompt))
            ))
        ); //Creamos un mapa con la estructura que espera la API

        //llamamos a la api de gemini
        return webClient.post().uri(apiUrl)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE) //Especificamos el tipo de contenido, en este caso JSON
        .bodyValue(requestBody)
        .retrieve()
        .bodyToMono(String.class) // Convertimos la respuesta en String (puede ajustarse según el JSON de Gemini)
        .block();
    }


     //Función para generar un tip que mejores los hábitos en general del user
     public String generateTip(Map<String, Float> sleepLogsLastWeek,Map<Long, SleepLogAnswer> sleepLogsForContext, OnboardingAnswerDTO onboardingAnswerDTO, DrmObjectDTO drmObjectDTO, User user){
        String apiUrl = "/models/gemini-1.5-flash:generateContent?key=" + apiKey; // Cambiar el modelo a 1.5 Flash

        //PROMPT PARA INDICARLE EL FORMATO DEL INFORME DE LA TOMA DE DECISIONES Y QUE ES LO QUE TIENE QUE TENER EN CUENTA PARA HACERLO
        String prompt = """
        A continuación, se te entrega la siguiente información con el objetivo de generar un informe semanal profesional que analice cómo el sueño impacta en la calidad de la toma de decisiones del usuario. La información se compone de varios bloques:

        1. Registro de sueño de la última semana (sleepLogsLastWeek):
        - Es un mapa donde cada clave representa un día de la semana (por ejemplo, "Lunes", "Martes", etc.) y el valor asociado es el número de horas que el usuario durmió ese día.
        - Nota: Un valor de 0 indica que el usuario no registró las horas de sueño en ese día.

        2. Respuestas del Onboarding (OnboardingAnswerDTO):
        Este objeto contiene las respuestas a preguntas relacionadas con los hábitos de sueño y estilo de vida del usuario:
            - question1 ("¿Cuántas horas sueles dormir?"):
                    Opciones disponibles:
                    • "Menos de 5 horas"
                    • "Entre 5 y 6 horas"
                    • "Entre 6 y 7 horas"
                    • "Entre 7 y 8 horas"
                    • "Más de 8 horas"
            - question2 ("¿Cuántos días haces actividad física a la semana?"):
                    Opciones:
                    • "Ninguno"
                    • "Entre 1 y 2 días"
                    • "Entre 3 y 4 días"
                    • "Entre 5 y 6 días"
                    • "Todos los días"
            - question3:
                    Se espera un valor numérico (por ejemplo, "24") que representa la edad del usuario. Este dato se utiliza para determinar en qué rango de edad se encuentra el usuario y así personalizar el informe.
            - question4 ("¿Qué tipo de alimentación llevas?"):
                    Opciones:
                    • "Omnívora"
                    • "Vegetariana"
                    • "Vegana"
                    • "Flexitariana"
                    • "Otro"
            - question5 ("¿Cuál es tu nivel de estrés diario?"):
                    Opciones:
                    • "Muy bajo"
                    • "Bajo"
                    • "Moderado"
                    • "Alto"
                    • "Muy alto"

        3. Respuesta del cuestionario diario DRM (drmObjectDTO):
        Este objeto recopila el informe generado de la toma de decisiones del día actual:
            - id (Solo representa la id del informe en la base de datos asi que de esto puedes prescindir)
            - timeStamp: La fecha y hora en la que se generó el informe.(De esto puedes prescindir)
            - report: El informe generado por la IA, que incluye un análisis de la toma de decisiones del usuario.

        4. Registro completo de sueño para contexto (sleepLogsForContext):
        Este mapa ofrece detalles adicionales de los registros de sueño del usuario y contiene los siguientes campos en cada registro:
            - wakeuptime: La hora a la que el usuario se despertó.
            - sleepTime: La hora a la que el usuario se fue a dormir.
            - duration: La duración total del sueño.
            - answer1: Respuesta a la pregunta "¿Cómo ha sido la calidad de tu sueño?"
                        Posibles valores: "Muy buena", "Buena", "Regular", "Mala", "Muy mala".
            - answer2: Respuesta a la pregunta "¿Cómo te sientes de descansado?"
                        Posibles valores: "Muy descansado", "Descansado", "Ni descansado ni cansado", "Cansado", "Muy cansado".

        5. Recomendaciones de sueño (sleepRecommendations):
        Se proporcionan recomendaciones de sueño basadas en rangos de edad. Cada entrada incluye:
            - ageRange: El rango de edad al que aplica (por ejemplo, "6-13", "14-17", "18-25", "26-64", "65+").
            - hours: Valores fijos (plantilla) para cada rango de edad:
                    • Para "6-13":     minHours = 9,   idealHours = 10,  maxHours = 11.
                    • Para "14-17":    minHours = 8,   idealHours = 9,   maxHours = 10.
                    • Para "18-25":    minHours = 7,   idealHours = 8,   maxHours = 9.
                    • Para "26-64":    minHours = 7,   idealHours = 8,   maxHours = 9.
                    • Para "65+":      minHours = 7,   idealHours = 7.5, maxHours = 8.
            - phases: (Información sobre las fases del sueño que se registra, pero no es necesario analizarla ya que aún no está implementado.)

        Propósito del Informe:
        El objetivo es generar un breve informe semanal que analice de forma profesional cómo la duración y calidad del sueño, junto con otros factores (actividad física, alimentación, nivel de estrés y concentración), impactan en la calidad de la toma de decisiones del usuario. Se espera que el informe integre la información de los registros diarios, las respuestas del onboarding y las respuestas relacionadas con la toma de decisiones, proporcionando una visión integral del estado del sueño y su influencia en el desempeño diario.
        El informe tiene que tener tanto una buena argumentación como un buen análisis de la toma de decisiones del user en base a la información que has recibido pasado.
        Está bien que menciones algunas de las respuestas que el user ha puesto en los distintos cuestionarios pero haz la redacción del informe de manera profesional y cercana, como si fuese un informe de un profesional de la salud que se lo entrega a un paciente.
        Y que dicha redacción sea de una forma fluída.

        Resumen del objetivo para que lo puedas entender mejor:
        Utilizando estos datos, genera un informe semanal en texto plano, sin títulos, encabezados o secciones (pero si que puedes usar parrafos). El informe debe ser un análisis profesional y cercano sobre cómo la duración y calidad del sueño han impactado la toma de decisiones del usuario, indicando de manera clara y fundamentada qué aspectos podría estar haciendo mal o qué problemas podría estar presentando.
        Incluye ejemplos y valores específicos cuando sea relevante.
        QUIERO QUE HAGAS UNA CONCLUSIÓN COMPLETA Y PROFESIONAL DE COMO TODA LA INFO QUE TE DA EL USER IMPACTA EN LA TOMA DE DECISIONES, NO QUIERO QUE LE DES RECOMENDACIONES PARA MEJORAR LA SITUACIÓN PQ DE ESTO
        SE ENCARGARÁ OTRA PERSONA. Al empezar el informe si quieres te puedes referir al usario pero de una manera profesional.

        Además, se pasa la edad del usuario (en el valor ageUser) para determinar en qué rango de edad se encuentra, lo que permite personalizar el informe y hacerlo más profesional.
        
        Ya tienes la estructura para que entiendas la info que se te pasa del user, te la paso a continuación:
        sleepLogsLastWeek: %s
        onboardingAnswerDTO: %s
        drmObjectDTO: %s
        sleepLogsForContext: %s
        fecha de nacimiento: %s
        edad (Si vale -1 significa que la fech de nacimiento es null): %s
        userName: %s

        Por favor, responde exclusivamente con un objeto JSON que contenga los siguientes campos y valores, respetando estrictamente el formato indicado y sin incluir ningún comentario, explicación o contenido extra. La respuesta debe ser un JSON válido y utilizar un tono profesional
        que ayude aluser a mejorar su calidad de vida y hábitos, y que esteobjeto no esté repetido en la lista de tips que ya tiene el user. El JSON debe contener los siguientes campos (ES UN EJEMPLO PARA QUE SEPAS QUE ESTRUCTURA TIENES QUE DEVOLVER):
        {
            "title": "Título del tip",
            "description": "Descripción breve del tip",
            "icon": "icono del tip",
            "fullDescription": "Descripción completa del tip",
            "benefits": [
                "Beneficio 1",
                "Beneficio 2"
            ],
            "steps": [
                "Paso 1",
                "Paso 2"
            ]
        }
        """.formatted(sleepLogsLastWeek.toString(), onboardingAnswerDTO.toString(), drmObjectDTO.toString(), sleepLogsForContext.toString(), user.getBirthDate().toString(), String.valueOf(user.getAge()) , user.getName());
        

        System.out.println(prompt);

        //Construímos el cuerpo de la petición
        Map<String, Object> requestBody = Map.of(
            "contents", List.of(Map.of(
                "parts", List.of(Map.of("text", prompt))
            ))
        ); //Creamos un mapa con la estructura que espera la API

        //llamamos a la api de gemini
        return webClient.post().uri(apiUrl)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE) //Especificamos el tipo de contenido, en este caso JSON
        .bodyValue(requestBody)
        .retrieve()
        .bodyToMono(String.class) // Convertimos la respuesta en String (puede ajustarse según el JSON de Gemini)
        .block();
    }
   
}
