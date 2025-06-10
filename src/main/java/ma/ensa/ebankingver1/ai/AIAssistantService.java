package ma.ensa.ebankingver1.ai;
/*
import ma.ensa.ebankingver1.model.User;
import ma.ensa.ebankingver1.service.BankAccountService;
import ma.ensa.ebankingver1.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AIAssistantService {
    private static final Logger logger = LoggerFactory.getLogger(AIAssistantService.class);

    @Autowired
    private WitAIClient witAIClient;

    @Autowired
    private UserService userService;

    @Autowired
    private BankAccountService bankAccountService;

    @Autowired
    private LibreTranslateClient libreTranslateClient;

    public AIResponse processClientRequest(String clientId, String request, String language) {
        try {
            logger.info("Processing request for clientId: {}, request: {}, language: {}", clientId, request, language);

            // Retrieve user for personalization
            User user = userService.findById(Long.parseLong(clientId));
            if (user == null) {
                logger.error("User not found for clientId: {}", clientId);
                return new AIResponse(getDefaultErrorMessage(language), false);
            }
            String userName = (user.getFirstName() != null && !user.getFirstName().isEmpty()) ?
                    user.getFirstName().trim() : "Client";

            // Call Wit.ai to analyze the request
            WitAIResponse witResponse = witAIClient.processRequest(request);
            if (witResponse == null) {
                logger.error("Wit.ai returned null response for request: {}", request);
                return new AIResponse(getDefaultErrorMessage(language), false);
            }
            String intent = witResponse.getIntent();
            logger.info("Intent detected: {}", intent);

            // Default response
            String responseText = getDefaultUnknownMessage(userName, language);

            // Process intents
            switch (intent != null ? intent : "") {
                case "check_balance":
                    var accounts = bankAccountService.getAccountsByClientId(Long.parseLong(clientId));
                    if (accounts == null || accounts.isEmpty()) {
                        logger.error("No accounts found for clientId: {}", clientId);
                        return new AIResponse(getNoAccountMessage(language), false);
                    }
                    String accountId = accounts.get(0).getId();
                    double balance = bankAccountService.getBankAccount(accountId).getBalance();
                    responseText = String.format(getBalanceMessageFormat(language), userName, balance);
                    break;

                case "recharge_phone":
                    Map<String, Object> entities = witResponse.getEntities();
                    logger.debug("Entities received: {}", entities);
                    double amount = entities.containsKey("amount_of_money") ?
                            ((Number) entities.get("amount_of_money")).doubleValue() : 0.0;
                    String phoneNumber = entities.containsKey("phone_number") ?
                            String.valueOf(entities.get("phone_number")) :
                            String.valueOf(entities.getOrDefault("destination_account", "non spécifié"));
                    responseText = String.format(getRechargeMessageFormat(language), userName, amount, phoneNumber);
                    break;

                case "referral_info":
                    responseText = String.format(getReferralMessageFormat(language), userName);
                    break;

                default:
                    responseText = String.format(getDefaultUnknownMessage(userName, language), userName);
            }

            // Handle multilingual support
            responseText = translateIfNeeded(responseText, language);

            return new AIResponse(responseText, true,intent);


        } catch (Exception e) {
            logger.error("Error processing AI request for clientId: {}, request: {}. Exception: {}", clientId, request, e.getMessage(), e);
            return new AIResponse(getDefaultErrorMessage(language) + ": " + e.getMessage(), false);
        }
    }

    private String translateIfNeeded(String text, String targetLanguage) {
        if (targetLanguage == null || targetLanguage.equals("fr")) {
            logger.info("No translation needed, language: fr");
            return text;
        }
        try {
            String sourceLang = "fr";
            String targetLang = targetLanguage.equals("ar") ? "ar" : targetLanguage.equals("en") ? "en" : "fr";
            if (targetLang.equals("fr")) {
                return text;
            }
            logger.info("Translating text to {}", targetLang);
            String translated = libreTranslateClient.translate(text, sourceLang, targetLang);
            logger.info("Translation result: {}", translated);
            return translated;
        } catch (Exception e) {
            logger.error("Translation failed for language {}: {}", targetLanguage, e.getMessage());
            return text;
        }
    }

    private String getDefaultErrorMessage(String language) {
        return switch (language) {
            case "ar" -> "حدث خطأ في معالجة طلبك.";
            case "en" -> "An error occurred while processing your request.";
            default -> "Une erreur s'est produite lors du traitement de votre demande.";
        };
    }

    private String getNoAccountMessage(String language) {
        return switch (language) {
            case "ar" -> "لم يتم العثور على حساب بنكي.";
            case "en" -> "No bank account found.";
            default -> "Aucun compte bancaire trouvé.";
        };
    }

    private String getDefaultUnknownMessage(String userName, String language) {
        return switch (language) {
            case "ar" -> "مرحبًا %s، لم أفهم طلبك بعد، لكنني أتحسن!";
            case "en" -> "Hello %s, I don’t understand your request yet, but I’m improving!";
            default -> "Bonjour %s, je ne comprends pas encore cette demande, mais je m’améliore !";
        };
    }

    private String getBalanceMessageFormat(String language) {
        return switch (language) {
            case "ar" -> "مرحبًا %s، رصيدك هو %.2f درهم مغربي.";
            case "en" -> "Hello %s, your balance is %.2f MAD.";
            default -> "Bonjour %s, votre solde est de %.2f MAD.";
        };
    }

    private String getRechargeMessageFormat(String language) {
        return switch (language) {
            case "ar" -> "مرحبًا %s، تم استلام طلب إعادة شحن %.2f درهم مغربي لرقم %s. يرجى التأكيد عبر التطبيق.";
            case "en" -> "Hello %s, your request to recharge %.2f MAD for the number %s has been received. Please confirm via the application.";
            default -> "Bonjour %s, votre demande de recharge de %.2f MAD pour le numéro %s est reçue. Veuillez confirmer via l’application.";
        };
    }

    private String getReferralMessageFormat(String language) {
        return switch (language) {
            case "ar" -> "مرحبًا %s، ادعُ صديقًا برابط الإحالة الخاص بك واكسب 50 درهمًا مغربيًا لكل منكما بعد إيداعه الأول! تحقق من ملفك الشخصي للحصول على الرابط.";
            case "en" -> "Hello %s, invite a friend with your referral link and earn 50 MAD each after their first deposit! Check your profile for the link.";
            default -> "Bonjour %s, invitez un ami avec votre lien de parrainage et gagnez 50 MAD chacun après son premier dépôt ! Consultez votre profil pour le lien.";
        };
    }
}

 */
