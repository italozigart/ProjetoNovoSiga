# Novo Siga

Sistema web de **gestão acadêmica** com **loja das atléticas**, feito com Spring Boot, Thymeleaf e PostgreSQL
(projeto acadêmico).

## Funcionalidades

**Cadastros (CRUD com validação)**
- Alunos (com foto), Professores, Cursos e Disciplinas
- Atléticas (com logo e curso representado)
- Produtos das atléticas: categoria, preço, **estoque** e imagem
- Validação dos formulários com mensagens em português (`@Valid` + Bean Validation),
  máscaras de CPF, telefone e CEP, e bloqueio de exclusão quando há registros ligados
  (ex.: curso com alunos, produto já vendido)

**Loja e carrinho de compras**
- Vitrine com os produtos das atléticas, página de detalhes e "mais produtos desta atlética"
- Carrinho guardado na sessão do usuário (`@SessionScope`): adicionar, alterar quantidade, remover e esvaziar
- Finalização da compra: escolha do aluno comprador e da forma de pagamento
- Baixa de estoque com **trava no banco** (`SELECT ... FOR UPDATE`) e transação: se faltar estoque nada é gravado
- Lista de pedidos, detalhes do pedido e pedido de balcão (tela com JavaScript/fetch)

**Encontrabilidade**
- **Busca geral** no topo de todas as páginas, com **sugestões enquanto digita** (autocompletar)
- Página de resultados agrupados por tipo (produtos, alunos, professores, disciplinas, cursos, atléticas, pedidos)
- **Barra de busca em todas as listagens** + paginação
- Busca que **ignora acentos e maiúsculas** ("joao" encontra "João") e aceita várias palavras
- CPF/telefone com ou sem pontuação; pedido pelo número ("12" ou "#12")
- Filtros da loja: atlética, categoria, faixa de preço, somente com estoque, ordenação
- Termo buscado **destacado** nos resultados, etiquetas de filtros ativos removíveis, menu com a página atual destacada

## Tecnologias

Java 17+ • Spring Boot 4 (Web MVC, Data JPA, Security, Validation) • Thymeleaf • PostgreSQL • Lombok • H2 (somente testes)

## Como executar

**Pré-requisitos:** JDK 17 ou mais novo e PostgreSQL rodando na porta 5432.

1. Crie o banco `db_novosiga` (pgAdmin → *Databases* → *Create* → *Database*, ou pelo terminal):
   ```
   psql -U postgres -c "CREATE DATABASE db_novosiga;"
   ```
2. Usuário e senha do banco ficam em `src/main/resources/application.properties`
   (padrão: `postgres` / `123456`). Também podem ser informados pelas variáveis de ambiente
   `DB_URL`, `DB_USER` e `DB_PASSWORD`.
3. Na pasta do projeto (onde está o `pom.xml`):
   - Windows (PowerShell): `.\mvnw.cmd spring-boot:run`
   - Linux/macOS: `chmod +x mvnw && ./mvnw spring-boot:run`
4. Acesse **http://localhost:8080**

Na primeira execução com o banco vazio, o sistema cria dados de exemplo (cursos, professores,
disciplinas, alunos, 3 atléticas e 12 produtos) e o usuário **admin / admin123**.
Para não criar nada, use `novosiga.dados-exemplo=false` no `application.properties`.

> As tabelas e colunas novas são criadas automaticamente (`ddl-auto=update`).
> Produtos cadastrados antes desta versão ficam com **estoque 0** e sem atlética:
> cadastre uma atlética e edite esses produtos para que apareçam disponíveis na loja.

### Testes automatizados

```
.\mvnw.cmd test
```
Usam o banco H2 em memória (não precisam do PostgreSQL).

## Principais endereços

| Endereço | Tela |
|---|---|
| `/novosiga` | Página de boas-vindas |
| `/login` • `/usuarios/criar` | Login e cadastro de usuário |
| `/home` | Tela inicial com atalhos e totais |
| `/loja` • `/loja/produto/{id}` | Vitrine e detalhes do produto |
| `/carrinho` • `/carrinho/finalizar` | Carrinho e finalização da compra |
| `/pedidos/listar` • `/pedidos/criar` | Pedidos e pedido de balcão |
| `/busca?q=...` | Busca geral |
| `/alunos/listar`, `/professores/listar`, `/cursos/listar`, `/disciplinas/listar`, `/atleticas/listar`, `/produtos/listar` | Cadastros |

## Estrutura do código

```
src/main/java/com/novosiga/novosiga
├── busca        Busca (consultas sem acento), ResultadoBusca, Sugestao
├── carrinho     Carrinho (@SessionScope), ItemCarrinho, ResumoCarrinho
├── config       DadosIniciais (dados de exemplo)
├── controller   Controllers MVC + GlobalControllerAdvice (carrinho/usuário em todas as telas)
├── dto          FiltroLoja, PedidoRequest, ItemPedidoRequest
├── entity       Aluno, Professor, Curso, Disciplina, Atletica, Produto, Pedido, ItemDoPedido, Usuario
├── exception    NegocioException (mensagens de regra de negócio)
├── repository   Spring Data JPA (+ JpaSpecificationExecutor para as buscas)
├── security     Spring Security (login com BCrypt)
├── service      Regras de negócio
└── util         TextoUtil, ImagemUtil, UrlUtil

src/main/resources
├── templates/fragments   layout.html (cabeçalho, menu, busca, paginação) e produto.html (card)
├── static/css/novosiga.css
└── static/js/novosiga.js  autocompletar, destaque, máscaras, carrinho
```
