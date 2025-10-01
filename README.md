# NovaBanking – Java Banking System

**NovaBanking** is a banking management system developed in **Java** with **MySQL (JDBC) integration**.  
It provides secure handling of clients, accounts, cards, and transactions, featuring **strong authentication, multi-currency operations, and persistent data storage**.

---

## 🔹 Key Features

**Client & Account Management**
- Current and savings accounts
- Unique IBANs
- Encrypted credentials (**SHA-256**)
- Current accounts (with type-based commission) and savings accounts (with interest by term)   

**Cards**
- Automatic issuance: VISA, MasterCard, AMEX, Maestro
- Status tracking: **Active, Blocked, Deleted**

**Security**
- Two-factor authentication with randomly generated **OTP**
- Single active session per user

**Multi-Currency Support**
- Balances in **RON, EUR, USD**
- Real-time conversion via [Open Exchange Rate API](https://open.er-api.com)

**Banking Operations**
- Deposits and withdrawals (**withdrawals only via active card**)
- Internal transfers with commission and currency conversion
- Real-time funds validation
- Account statements by date range
- Interest applied on savings accounts based on deposit term (3M, 6M, 1Y)
- Manual approval for large transactions (> 5000 RON equivalent, OTP secured)

**Bill Payments**
- One-time payments with commission
- Configurable recurring payments (provider, amount, frequency, currency, start date)
- Recurring payments are **stored in the database** and executed **manually** via the "Execute Recurring Payments" menu option
- Time simulation allows advancing the current date to trigger eligible recurring payments

**Audit & Logging**
- Authentication, card actions, large transfers, and payments recorded in log files

---
