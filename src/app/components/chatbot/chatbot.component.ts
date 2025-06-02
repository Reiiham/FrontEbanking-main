import { Component, OnInit, AfterViewChecked, ViewChild, ElementRef, Input } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AIAssistantService, AIResponse } from '../../services/ai-assistant.service';
import { FormsModule } from '@angular/forms';
import {CommonModule} from '@angular/common';


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
  }

  ngAfterViewChecked() {
    this.scrollToBottom();
  }

  toggleChat() {
    this.isOpen = !this.isOpen;
    if (this.isOpen && this.messages.length === 1) {
      this.addWelcomeMessage();
    }
  }

  initializeChat() {
    this.messages = [{
      id: this.messageId++,
      text: "👋 Bonjour ! Je suis votre assistant bancaire. Comment puis-je vous aider aujourd'hui ?",
      isUser: false,
      timestamp: new Date()
    }];
  }

  addWelcomeMessage() {
    setTimeout(() => {
      this.addBotMessage("Vous pouvez me demander :\n• Consulter votre solde\n• Voir vos dernières transactions\n• Faire un virement\n• Gérer vos bénéficiaires\n\nQue souhaitez-vous faire ?");
    }, 500);
  }

  sendMessage() {
    if (!this.currentMessage.trim()) return;

    this.addUserMessage(this.currentMessage);
    const userMessage = this.currentMessage;
    this.currentMessage = '';

    this.processUserMessageWithAI(userMessage);
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
    // Exemple de formatage : échappe les balises HTML pour éviter les injections
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
    console.log('🔧 AIAssistantService instance:', this.aiAssistantService);

    this.showTyping();

    this.aiAssistantService.processMessage(this.effectiveClientId, message, 'fr').subscribe({
      next: (response: AIResponse) => {
        console.log('✅ AI Response received in component:', response);
        console.log('📊 Response type:', typeof response);
        console.log('🔍 Response properties:', Object.keys(response));

        this.hideTyping();

        if (response && response.success) {
          console.log('✨ Processing successful AI response');
          console.log('📝 Response text:', response.responseText);
          console.log('🎯 Response intent:', response.intent);

          // Handle different intents based on AI response
          this.handleAIResponse(response, message);
        } else {
          console.log('⚠️ AI response indicates failure or is invalid');
          console.log('❌ Response success:', response?.success);
          console.log('❌ Response object:', response);

          const errorMessage = response?.responseText || 'Réponse invalide du serveur';
          this.addBotMessage("❌ " + errorMessage);
        }
      },
      error: (error) => {
        console.error('❌ Error in processUserMessageWithAI subscription:', error);
        console.error('❌ Error type:', typeof error);
        console.error('❌ Error constructor:', error.constructor?.name);
        console.error('❌ Error status:', error.status);
        console.error('❌ Error statusText:', error.statusText);
        console.error('❌ Error url:', error.url);
        console.error('❌ Error message:', error.message);

        if (error.error) {
          console.error('❌ Nested error object:', error.error);
          console.error('❌ Nested error type:', typeof error.error);
        }

        this.hideTyping();

        // More specific error message based on error type
        let errorMessage = "❌ Désolé, je rencontre des difficultés techniques. Veuillez réessayer.";

        if (error.status === 0) {
          errorMessage = "❌ Impossible de contacter le serveur. Vérifiez votre connexion.";
        } else if (error.status >= 400 && error.status < 500) {
          errorMessage = `❌ Erreur client (${error.status}): ${error.message}`;
        } else if (error.status >= 500) {
          errorMessage = `❌ Erreur serveur (${error.status}): ${error.message}`;
        }

        this.addBotMessage(errorMessage);
      }
    });
  }
  /**
   * Handle AI response and perform actions based on detected intent
   */
  handleAIResponse(response: AIResponse, originalMessage: string) {
    // Add the AI response text first
    this.addBotMessage(response.responseText);

    // Perform specific actions based on detected intent
    if (response.intent) {
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
          // AI has already provided a response, no additional action needed
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
          this.addBotMessage("❌ Aucun compte trouvé.");
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
        if (response.success && response.data.transactions.length > 0) {
          let message = "📊 **Vos dernières transactions :**\n\n";
          response.data.transactions.forEach((transaction: Transaction) => {
            const amount = transaction.amount > 0 ? `+${transaction.amount}€` : `${transaction.amount}€`;
            const date = new Date(transaction.operationDate).toLocaleDateString();
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
      this.addBotMessage(`💸 Vous souhaitez faire un virement de ${amount}€.\n\nPour des raisons de sécurité, les virements doivent être effectués depuis la section "Virements" de votre tableau de bord.\n\n🔐 **Étapes :**\n1. Allez dans "Mes Virements"\n2. Sélectionnez le bénéficiaire\n3. Saisissez le montant\n4. Confirmez avec votre code\n\nSouhaitez-vous que je vous montre vos bénéficiaires ?`);
    } else {
      this.addBotMessage("💸 Pour effectuer un virement, rendez-vous dans la section \"Virements\" de votre tableau de bord.\n\nVoulez-vous voir la liste de vos bénéficiaires ?");
    }
  }

  getBeneficiaries() {
    this.aiAssistantService.getBeneficiaries(this.effectiveClientId).subscribe({
      next: (beneficiaries) => {
        if (beneficiaries && beneficiaries.length > 0) {
          let message = "👥 **Vos bénéficiaires :**\n\n";
          beneficiaries.forEach((beneficiary: any) => {
            message += `• ${beneficiary.name} - ${beneficiary.bankName}\n`;
          });
          message += "\nPour ajouter un bénéficiaire, allez dans la section 'Bénéficiaires' de votre tableau de bord.";
          this.addBotMessage(message);
        } else {
          this.addBotMessage("👥 Vous n'avez pas encore de bénéficiaires enregistrés.\n\nPour en ajouter un, allez dans la section 'Bénéficiaires' de votre tableau de bord.");
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

💰 **Comptes**
• "Quel est mon solde ?"
• "Montre-moi mes comptes"

📊 **Transactions**
• "Mes dernières transactions"
• "Historique du compte"

💸 **Virements**
• "Faire un virement"
• "Mes bénéficiaires"

❓ **Autres**
• "Aide" pour ce menu
• Tapez votre question naturellement

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
}

