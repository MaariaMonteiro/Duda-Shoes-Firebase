<h1>👟🔥 DudaShoes – App com Firebase</h1>

<h2>🗂️ Visão Geral</h2>
<p>
  O DudaShoes é um aplicativo integrado ao Firebase, D=desenvolvido com Kotlin e Jetpack Compose, que oferece todas as funcionalidades de um CRUD completo para gerenciamento de produtos e usuários.
</p>

<h3>🛠️ Funcionalidades Principais</h3>

<ol>
  <li>✔️ Cadastro de Tênis</li>
  <li>✔️ Edição de Produtos</li>
  <li>✔️ Exclusão de Itens</li>
  <li>✔️ Visualização de Lista de Tênis</li>
  <li>✔️ Cadastro, Visualização, Edição e Exclusão de Usuários</li>
  <li>✔️ Integração Total com Firebase (Firestore + Authentication)</li>
</ol>

---

<h2>📱 Telas Iniciais</h2>

<img src="https://github.com/user-attachments/assets/42c96e02-1148-4f5b-ac2d-e6c489f8bf88" width="300">
<img src="https://github.com/user-attachments/assets/84b59e4f-5466-4760-baab-9e9d22774359" width="300">

<br><br>

---

<h2>1. 🔐 Tela de Cadastro</h2>

<img src="https://github.com/user-attachments/assets/0e5c8cbc-100d-4a4b-8f3a-acfcdfacb53c" width="300">

<ol>
  <li>Cria usuário com <code>auth.createUserWithEmailAndPassword()</code></li>
  <li>Salva dados pessoais no Firestore</li>
  <li>Navega para login após cadastro</li>
</ol>

---

<h2>2. 🔐 Tela de Login</h2>

<img src="https://github.com/user-attachments/assets/9a577e8b-c4f4-488a-99c2-1ac477259ce9" width="300">
<img src="https://github.com/user-attachments/assets/8f4b5e13-74e7-4c3a-864f-eb8c29300519" width="1700">
<img src="https://github.com/user-attachments/assets/710f0beb-7677-486d-b645-dfd09870731d" width="1700">

<ul>
  <li>Valida email/senha com Firebase Authentication</li>
  <li>Busca apelido do usuário no Firestore</li>
  <li>Navega para home com apelido do usuário</li>
</ul>

---

<h2>3. 🏠 Tela Principal com Menu</h2>

<img src="https://github.com/user-attachments/assets/59f342f1-f2e7-46ae-a88d-7a7fd096de74" width="300">
<img src="https://github.com/user-attachments/assets/0678a343-c035-4eb6-9fc0-d988a5890205" width="300">

<ul>
  <li>Menu lateral com navegação entre seções</li>
  <li>Dashboard inicial com boas-vindas</li>
  <li>Acesso a todas as funcionalidades do app</li>
</ul>

<br>

---

<h2>4. 📊 Lista de Registros (Usuários)</h2>

<img src="https://github.com/user-attachments/assets/188f09c9-5c55-4223-8ef6-b29b3745f6fa" width="300">
<img src="https://github.com/user-attachments/assets/85f3a786-e82d-43a0-8d69-abd38dfe974a" width="300">
<img src="https://github.com/user-attachments/assets/11634b4c-68fa-490f-beef-d2517c5cdce1" width="300">

<ul>
  <li>Lista todos os usuários cadastrados no Firestore</li>
  <li><strong>Editar Usuário</strong>: Modal para modificar dados</li>
  <li><strong>Excluir Usuário</strong>: Confirmação antes de deletar</li>
  <li>Operações usam <code>document().update()</code> e <code>document().delete()</code></li>
</ul>

<br>

---

<h2>5. 👟 Lista de Calçados</h2>

<img src="https://github.com/user-attachments/assets/90688827-ce95-4a11-a242-c769bab40868" width="300">
<img src="https://github.com/user-attachments/assets/6f424ffd-2b9f-4040-a9ad-f2d7631c0a29" width="300">

<ul>
  <li>Exibe todos os produtos da coleção <code>calcados</code></li>
  <li><strong>Adicionar Calçado</strong>: Dialog para cadastrar novo item</li>
  <li>Cards com imagem, nome, marca, preço, tamanho e descrição</li>
  <li><strong>Para editar e deletar</strong>: Mesma interface visual da tela de registros, agora usando a tabela de calçados</li>
</ul>

---
