
import { Component, OnInit, AfterViewChecked, ViewChild, ElementRef, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { AIAssistantService, AIResponse } from '../../services/ai-assistant.service';

interface Message {
  id: number;
  text: string;
  isUser: boolean;
  timestamp: Date;
  isTyping?: boolean;
}

interface BankAccount {
  id: string;
  balance: number;
  accountNumber: string;
  accountType: string;
}

interface Transaction {
  id: string;
  amount: number;
  description: string;
  operationDate: string;
  type: string;
}

@Component({
  selector: 'app-chatbot',
  templateUrl: './chatbot.component.html',
  styleUrls: ['./chatbot.component.scss'],
  standalone: true,
  imports: [CommonModule, FormsModule],
})
export class ChatbotComponent implements OnInit, AfterViewChecked {
  @ViewChild('messagesContainer') messagesContainer!: ElementRef;
  @Input() clientId: string | null = null; // Accept null values

  isOpen = false;
  messages: Message[] = [];
  currentMessage = '';
  isTyping = false;
  private messageId = 1;
  private effectiveClientId: string = ''; // Internal working clientId
  isAssistantAvailable = true; // Track if assistant service is available

  constructor(
    private http: HttpClient,
    private aiAssistantService: AIAssistantService
  ) {}

  ngOnInit() {
    // Handle clientId properly
    this.effectiveClientId = this.clientId || '123'; // Fallback to default

    if (!this.clientId) {
      console.warn('ChatbotComponent: No clientId provided, using fallback');
    }

    this.initializeChat();
    this.checkAssistantHealth();
  }

  ngAfterViewChecked() {
    this.scrollToBottom();
  }

  /**
   * Check if the assistant service is healthy
   */
  checkAssistantHealth() {
    this.aiAssistantService.checkAssistantHealth().subscribe({
      next: (response) => {
        this.isAssistantAvailable = response.modelAvailable;
        console.log('🏥 Assistant health status:', response);

        if (!this.isAssistantAvailable) {
          console.warn('⚠️ AI Assistant model is not available');
        }
      },
      error: (error) => {
        console.error('❌ Failed to check assistant health:', error);
        this.isAssistantAvailable = false;
      }
    });
  }

  toggleChat() {
    this.isOpen = !this.isOpen;
    if (this.isOpen && this.messages.length === 1) {
      this.addWelcomeMessage();
    }
  }

  initializeChat() {
    const welcomeMessage = this.isAssistantAvailable
      ? "👋 Bonjour ! Je suis votre assistant bancaire IA. Comment puis-je vous aider aujourd'hui ?"
      : "👋 Bonjour ! Je suis votre assistant bancaire. Comment puis-je vous aider aujourd'hui ?";

    this.messages = [{
      id: this.messageId++,
      text: welcomeMessage,
      isUser: false,
      timestamp: new Date()
    }];
  }

  addWelcomeMessage() {
    setTimeout(() => {
      const helpMessage = `Vous pouvez me demander :
• 💰 Consulter votre solde
• 📊 Voir vos dernières transactions
• 💸 Faire un virement
• 👥 Gérer vos bénéficiaires
• ❓ Obtenir de l'aide

${this.isAssistantAvailable ? 'Vous pouvez aussi me parler naturellement !' : ''}

Que souhaitez-vous faire ?`;

      this.addBotMessage(helpMessage);
    }, 500);
  }

  sendMessage() {
    if (!this.currentMessage.trim()) return;

    this.addUserMessage(this.currentMessage);
    const userMessage = this.currentMessage;
    this.currentMessage = '';

    // Use AI assistant if available, otherwise use simple pattern matching
    if (this.isAssistantAvailable) {
      this.processUserMessageWithAI(userMessage);
    } else {
      this.processUserMessageWithPatterns(userMessage);
    }
  }

  addUserMessage(text: string) {
    this.messages.push({
      id: this.messageId++,
      text,
      isUser: true,
      timestamp: new Date()
    });
  }

  addBotMessage(text: string) {
    this.messages.push({
      id: this.messageId++,
      text,
      isUser: false,
      timestamp: new Date()
    });
  }

  showTyping() {
    this.isTyping = true;
    this.messages.push({
      id: this.messageId++,
      text: '',
      isUser: false,
      timestamp: new Date(),
      isTyping: true
    });
  }

  hideTyping() {
    this.isTyping = false;
    this.messages = this.messages.filter(msg => !msg.isTyping);
  }

  formatMessage(message: string): string {
    // Escape HTML tags to prevent injection
    const div = document.createElement('div');
    div.textContent = message;
    return div.innerHTML;
  }

  /**
   * Process user message using AI assistant service
   */
  processUserMessageWithAI(message: string) {
    console.log('🤖 processUserMessageWithAI called with message:', message);
    console.log('🆔 Using effectiveClientId:', this.effectiveClientId);

    this.showTyping();

    this.aiAssistantService.processMessage(this.effectiveClientId, message, 'fr').subscribe({
      next: (response: AIResponse) => {
        console.log('✅ AI Response received in component:', response);

        this.hideTyping();

        if (response && response.success) {
          console.log('✨ Processing successful AI response');
          console.log('📝 Response text:', response.responseText);
          console.log('🎯 Response intent:', response.intent);

          // Handle different intents based on AI response
          this.handleAIResponse(response, message);
        } else {
          console.log('⚠️ AI response indicates failure');

          // Check if it's a service availability issue
          if (response?.responseText?.includes('Service ASSISTANT non activé')) {
            this.isAssistantAvailable = false;
            this.addBotMessage("⚠️ Le service d'assistant IA n'est pas activé pour votre compte. Je peux quand même vous aider avec les fonctions de base !");
            // Fallback to pattern matching
            this.processUserMessageWithPatterns(message);
          } else {
            const errorMessage = response?.responseText || 'Réponse invalide du serveur';
            this.addBotMessage("❌ " + errorMessage);
          }
        }
      },
      error: (error) => {
        console.error('❌ Error in processUserMessageWithAI:', error);
        this.hideTyping();

        // Handle specific error cases
        if (error.status === 403) {
          this.isAssistantAvailable = false;
          this.addBotMessage("⚠️ Service d'assistant IA non disponible. Je vais vous aider avec les fonctions de base.");
          this.processUserMessageWithPatterns(message);
        } else if (error.status === 0) {
          this.addBotMessage("❌ Impossible de contacter le serveur. Vérifiez votre connexion.");
        } else if (error.status >= 400 && error.status < 500) {
          this.addBotMessage(`❌ Erreur client (${error.status}). Veuillez réessayer.`);
        } else if (error.status >= 500) {
          this.addBotMessage(`❌ Erreur serveur (${error.status}). Le service est temporairement indisponible.`);
        } else {
          this.addBotMessage("❌ Désolé, je rencontre des difficultés techniques. Veuillez réessayer.");
        }
      }
    });
  }

  /**
   * Fallback method using simple pattern matching when AI is not available
   */
  processUserMessageWithPatterns(message: string) {
    console.log('🔍 Using pattern matching for message:', message);

    const lowerMessage = message.toLowerCase();

    this.showTyping();

    // Simulate thinking time
    setTimeout(() => {
      this.hideTyping();

      if (lowerMessage.includes('solde') || lowerMessage.includes('balance')) {
        this.addBotMessage("💰 Je vais récupérer vos soldes de compte...");
        this.getAccountBalance();
      } else if (lowerMessage.includes('transaction') || lowerMessage.includes('historique')) {
        this.addBotMessage("📊 Voici vos dernières transactions...");
        this.getTransactions();
      } else if (lowerMessage.includes('virement') || lowerMessage.includes('transfer')) {
        this.addBotMessage("💸 Pour les virements, je vais vous expliquer la procédure...");
        this.handleTransferRequest(message);
      } else if (lowerMessage.includes('bénéficiaire') || lowerMessage.includes('beneficiary')) {
        this.addBotMessage("👥 Voici vos bénéficiaires enregistrés...");
        this.getBeneficiaries();
      } else if (lowerMessage.includes('aide') || lowerMessage.includes('help') || lowerMessage.includes('menu')) {
        this.showHelp();
      } else {
        this.addBotMessage(`Je comprends que vous souhaitez "${message}". Voici ce que je peux faire pour vous aider :`);
        this.showHelp();
      }
    }, 800);
  }

  /**
   * Handle AI response and perform actions based on detected intent
   */
  handleAIResponse(response: AIResponse, originalMessage: string) {
    // Add the AI response text first
    this.addBotMessage(response.responseText);

    // Perform specific actions based on detected intent
    if (response.intent) {
      console.log('🎯 Handling intent:', response.intent);

      switch (response.intent.toLowerCase()) {
        case 'check_balance':
        case 'account_balance':
          this.getAccountBalance();
          break;

        case 'get_transactions':
        case 'transaction_history':
          this.getTransactions();
          break;

        case 'transfer_money':
        case 'wire_transfer':
          this.handleTransferRequest(originalMessage);
          break;

        case 'get_beneficiaries':
        case 'beneficiary_list':
          this.getBeneficiaries();
          break;

        case 'help':
        case 'show_menu':
          this.showHelp();
          break;

        default:
          console.log('🤷 Unknown intent, AI response only');
          break;
      }
    }
  }

  getAccountBalance() {
    this.aiAssistantService.getAccountBalance(this.effectiveClientId).subscribe({
      next: (accounts) => {
        if (accounts && accounts.length > 0) {
          let response = "💰 **Vos soldes de comptes :**\n\n";
          accounts.forEach((account: BankAccount) => {
            response += `• ${account.accountType || 'Compte'} (${account.accountNumber}): **${account.balance}€**\n`;
          });
          this.addBotMessage(response);
        } else {
          this.addBotMessage("ℹ️ Aucun compte trouvé.");
        }
      },
      error: (error) => {
        console.error('Error fetching accounts:', error);
        this.addBotMessage("❌ Désolé, je n'arrive pas à récupérer les informations de vos comptes en ce moment.");
      }
    });
  }

  getTransactions() {
    this.aiAssistantService.getTransactions(this.effectiveClientId, 0, 5).subscribe({
      next: (response) => {
        if (response.success && response.data && response.data.transactions && response.data.transactions.length > 0) {
          let message = "📊 **Vos dernières transactions :**\n\n";
          response.data.transactions.forEach((transaction: Transaction) => {
            const amount = transaction.amount > 0 ? `+${transaction.amount}€` : `${transaction.amount}€`;
            const date = new Date(transaction.operationDate).toLocaleDateString('fr-FR');
            message += `• ${date}: ${transaction.description} - **${amount}**\n`;
          });
          this.addBotMessage(message);
        } else {
          this.addBotMessage("ℹ️ Aucune transaction récente trouvée.");
        }
      },
      error: (error) => {
        console.error('Error fetching transactions:', error);
        this.addBotMessage("❌ Impossible de récupérer vos transactions pour le moment.");
      }
    });
  }

  handleTransferRequest(message: string) {
    // Extract amount if mentioned
    const amountMatch = message.match(/(\d+)/);
    if (amountMatch) {
      const amount = amountMatch[1];
      this.addBotMessage(`💸 Vous souhaitez faire un virement de ${amount}€.

🔐 **Pour des raisons de sécurité, les virements doivent être effectués depuis votre tableau de bord :**

**Étapes :**
1. Allez dans "Mes Virements"
2. Sélectionnez le bénéficiaire
3. Saisissez le montant
4. Confirmez avec votre code

Souhaitez-vous que je vous montre vos bénéficiaires ?`);
    } else {
      this.addBotMessage(`💸 **Pour effectuer un virement :**

Rendez-vous dans la section "Virements" de votre tableau de bord.

Voulez-vous voir la liste de vos bénéficiaires ?`);
    }
  }

  getBeneficiaries() {
    this.aiAssistantService.getBeneficiaries(this.effectiveClientId).subscribe({
      next: (beneficiaries) => {
        if (beneficiaries && beneficiaries.length > 0) {
          let message = "👥 **Vos bénéficiaires :**\n\n";
          beneficiaries.forEach((beneficiary: any) => {
            message += `• ${beneficiary.name} - ${beneficiary.bankName || beneficiary.bank || 'Banque non spécifiée'}\n`;
          });
          message += "\n💡 Pour ajouter un bénéficiaire, allez dans la section 'Bénéficiaires' de votre tableau de bord.";
          this.addBotMessage(message);
        } else {
          this.addBotMessage(`👥 **Aucun bénéficiaire enregistré**

Vous n'avez pas encore de bénéficiaires enregistrés.

💡 Pour en ajouter un, allez dans la section 'Bénéficiaires' de votre tableau de bord.`);
        }
      },
      error: (error) => {
        console.error('Error fetching beneficiaries:', error);
        this.addBotMessage("❌ Impossible de récupérer la liste de vos bénéficiaires.");
      }
    });
  }

  showHelp() {
    this.addBotMessage(`🤖 **Je peux vous aider avec :**

💰 **Comptes & Soldes**
• "Quel est mon solde ?"
• "Montre-moi mes comptes"

📊 **Transactions**
• "Mes dernières transactions"
• "Historique du compte"

💸 **Virements**
• "Faire un virement"
• "Mes bénéficiaires"

❓ **Aide**
• "Aide" pour ce menu
• "Menu" pour les options

${this.isAssistantAvailable ? '🤖 **Vous pouvez aussi me parler naturellement !** Posez vos questions comme vous le feriez à un conseiller.' : ''}

Comment puis-je vous aider ?`);
  }

  onKeyPress(event: KeyboardEvent) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  scrollToBottom() {
    if (this.messagesContainer) {
      const element = this.messagesContainer.nativeElement;
      element.scrollTop = element.scrollHeight;
    }
  }

  getMessageTime(timestamp: Date): string {
    return new Date(timestamp).toLocaleTimeString('fr-FR', {
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  // Debug method to test connection
  testConnection() {
    console.log('🧪 Testing assistant connection...');
    this.aiAssistantService.testConnection(this.effectiveClientId, "test de connexion").subscribe({
      next: (response) => {
        console.log('✅ Connection test successful:', response);
        this.addBotMessage("✅ Test de connexion réussi ! L'assistant IA est opérationnel.");
      },
      error: (error) => {
        console.error('❌ Connection test failed:', error);
        this.addBotMessage("⚠️ Test de connexion échoué. Fonctionnement en mode basique.");
        this.isAssistantAvailable = false;
      }
    });
  }
}
