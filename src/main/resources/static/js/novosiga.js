/* =========================================================
   NOVO SIGA • JavaScript compartilhado por todas as telas
   1. Autocompletar da busca geral (cabeçalho)
   2. Destaque do termo buscado nos resultados
   3. Confirmação antes de excluir
   4. Máscaras de CPF, telefone e CEP
   5. Prévia da imagem escolhida no upload
   6. Fechar alertas
   7. Enviar o formulário ao mudar um campo (ordenação, quantidade)
   8. Botões + e - de quantidade
   9. Filtro de opções de um select (escolha do aluno na finalização)
   ========================================================= */
(function () {
    'use strict';

    // Remove acentos e deixa minúsculo: "João" -> "joao"
    function semAcento(texto) {
        return (texto || '').normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase();
    }

    // Espera o usuário parar de digitar antes de executar
    function esperar(funcao, milissegundos) {
        let timer = null;
        return function () {
            const args = arguments;
            clearTimeout(timer);
            timer = setTimeout(function () { funcao.apply(null, args); }, milissegundos);
        };
    }

    /* ---------------------------------------------------------
       1. AUTOCOMPLETAR DA BUSCA GERAL
       Ao digitar 2+ letras, busca sugestões em /busca/sugestoes?q=...
       Setas ↑ ↓ navegam, Enter abre, Esc fecha.
       --------------------------------------------------------- */
    function iniciarAutocompletar() {
        const input = document.getElementById('busca-global');
        const lista = document.getElementById('sugestoes');
        if (!input || !lista) {
            return;
        }

        let sugestoes = [];
        let indiceAtivo = -1;
        let requisicao = null;

        function fechar() {
            lista.hidden = true;
            indiceAtivo = -1;
            input.setAttribute('aria-expanded', 'false');
        }

        function abrir() {
            lista.hidden = false;
            input.setAttribute('aria-expanded', 'true');
        }

        function marcar(indice) {
            const itens = lista.querySelectorAll('li[data-indice]');
            itens.forEach(function (li) { li.classList.remove('ativo'); });
            indiceAtivo = indice;
            if (indice >= 0 && itens[indice]) {
                itens[indice].classList.add('ativo');
                itens[indice].scrollIntoView({ block: 'nearest' });
            }
        }

        function criarItem(sugestao, indice) {
            const li = document.createElement('li');
            li.dataset.indice = String(indice);
            li.setAttribute('role', 'option');

            const link = document.createElement('a');
            link.href = sugestao.url;

            const icone = document.createElement('span');
            icone.className = 'sugestao-icone';
            const i = document.createElement('i');
            i.className = 'fa-solid ' + (sugestao.icone || 'fa-magnifying-glass');
            icone.appendChild(i);

            const texto = document.createElement('span');
            texto.className = 'sugestao-texto';
            const titulo = document.createElement('strong');
            titulo.textContent = sugestao.titulo || '';
            const detalhe = document.createElement('small');
            detalhe.textContent = sugestao.detalhe || '';
            texto.appendChild(titulo);
            texto.appendChild(detalhe);

            const tipo = document.createElement('span');
            tipo.className = 'sugestao-tipo';
            tipo.textContent = sugestao.tipo || '';

            link.appendChild(icone);
            link.appendChild(texto);
            link.appendChild(tipo);
            li.appendChild(link);
            return li;
        }

        function mostrar(dados, termo) {
            sugestoes = Array.isArray(dados) ? dados : [];
            indiceAtivo = -1;
            lista.innerHTML = '';

            if (sugestoes.length === 0) {
                const vazio = document.createElement('li');
                vazio.className = 'sugestao-vazia';
                vazio.textContent = 'Nenhuma sugestão para "' + termo + '". Pressione Enter para buscar em tudo.';
                lista.appendChild(vazio);
            }

            sugestoes.forEach(function (sugestao, indice) {
                lista.appendChild(criarItem(sugestao, indice));
            });

            const todos = document.createElement('li');
            todos.className = 'sugestao-todos';
            const link = document.createElement('a');
            link.href = '/busca?q=' + encodeURIComponent(termo);
            link.textContent = 'Ver todos os resultados para "' + termo + '"';
            todos.appendChild(link);
            lista.appendChild(todos);

            abrir();
        }

        const buscar = esperar(function () {
            const termo = input.value.trim();
            if (termo.length < 2) {
                fechar();
                return;
            }
            if (requisicao) {
                requisicao.abort();
            }
            requisicao = new AbortController();
            fetch('/busca/sugestoes?q=' + encodeURIComponent(termo), {
                signal: requisicao.signal,
                headers: { 'Accept': 'application/json' }
            })
                .then(function (resposta) { return resposta.ok ? resposta.json() : []; })
                .then(function (dados) {
                    if (input.value.trim() === termo) {
                        mostrar(dados, termo);
                    }
                })
                .catch(function () { /* requisição cancelada ou sessão expirada */ });
        }, 220);

        input.addEventListener('input', buscar);

        input.addEventListener('focus', function () {
            if (input.value.trim().length >= 2) {
                buscar();
            }
        });

        input.addEventListener('keydown', function (evento) {
            if (lista.hidden) {
                return;
            }
            const total = sugestoes.length;
            if (evento.key === 'ArrowDown' && total > 0) {
                evento.preventDefault();
                marcar((indiceAtivo + 1) % total);
            } else if (evento.key === 'ArrowUp' && total > 0) {
                evento.preventDefault();
                marcar((indiceAtivo - 1 + total) % total);
            } else if (evento.key === 'Enter' && indiceAtivo >= 0 && sugestoes[indiceAtivo]) {
                evento.preventDefault();
                window.location.href = sugestoes[indiceAtivo].url;
            } else if (evento.key === 'Escape') {
                fechar();
            }
        });

        document.addEventListener('click', function (evento) {
            if (!input.parentElement.contains(evento.target)) {
                fechar();
            }
        });
    }

    /* ---------------------------------------------------------
       2. DESTAQUE DO TERMO BUSCADO
       Elementos com class="destacavel" dentro de [data-destacar="termo"]
       recebem <mark> nas partes que combinam (ignorando acentos).
       --------------------------------------------------------- */
    function destacarTermos() {
        document.querySelectorAll('[data-destacar]').forEach(function (container) {
            const palavras = semAcento(container.getAttribute('data-destacar'))
                .split(/\s+/)
                .filter(function (palavra) { return palavra.length >= 2; });
            if (palavras.length === 0) {
                return;
            }
            container.querySelectorAll('.destacavel').forEach(function (elemento) {
                destacarElemento(elemento, palavras);
            });
        });
    }

    function destacarElemento(elemento, palavras) {
        const caminhante = document.createTreeWalker(elemento, NodeFilter.SHOW_TEXT);
        const nosDeTexto = [];
        while (caminhante.nextNode()) {
            nosDeTexto.push(caminhante.currentNode);
        }

        nosDeTexto.forEach(function (no) {
            const original = no.nodeValue;

            // Versão sem acento com o MESMO tamanho do texto original (letra por letra)
            let normalizado = '';
            for (let i = 0; i < original.length; i++) {
                const letra = semAcento(original[i]);
                normalizado += (letra.length === 1) ? letra : original[i];
            }

            // Encontra os trechos que combinam com alguma palavra
            const trechos = [];
            palavras.forEach(function (palavra) {
                let posicao = normalizado.indexOf(palavra);
                while (posicao !== -1) {
                    trechos.push([posicao, posicao + palavra.length]);
                    posicao = normalizado.indexOf(palavra, posicao + palavra.length);
                }
            });
            if (trechos.length === 0) {
                return;
            }

            // Junta trechos sobrepostos
            trechos.sort(function (a, b) { return a[0] - b[0]; });
            const unidos = [];
            trechos.forEach(function (trecho) {
                const ultimo = unidos[unidos.length - 1];
                if (ultimo && trecho[0] <= ultimo[1]) {
                    ultimo[1] = Math.max(ultimo[1], trecho[1]);
                } else {
                    unidos.push([trecho[0], trecho[1]]);
                }
            });

            // Remonta o texto com <mark>
            const fragmento = document.createDocumentFragment();
            let cursor = 0;
            unidos.forEach(function (trecho) {
                if (trecho[0] > cursor) {
                    fragmento.appendChild(document.createTextNode(original.slice(cursor, trecho[0])));
                }
                const marca = document.createElement('mark');
                marca.textContent = original.slice(trecho[0], trecho[1]);
                fragmento.appendChild(marca);
                cursor = trecho[1];
            });
            if (cursor < original.length) {
                fragmento.appendChild(document.createTextNode(original.slice(cursor)));
            }
            no.parentNode.replaceChild(fragmento, no);
        });
    }

    /* ---------------------------------------------------------
       3. CONFIRMAÇÃO ANTES DE EXCLUIR
       Qualquer link/botão com data-confirmar="mensagem"
       --------------------------------------------------------- */
    document.addEventListener('click', function (evento) {
        const elemento = evento.target.closest('[data-confirmar]');
        if (elemento && !window.confirm(elemento.getAttribute('data-confirmar'))) {
            evento.preventDefault();
        }
    });

    /* ---------------------------------------------------------
       4. MÁSCARAS (o servidor grava só os números)
       <input data-mascara="cpf|telefone|cep">
       --------------------------------------------------------- */
    const mascaras = {
        cpf: function (valor) {
            const d = valor.replace(/\D/g, '').slice(0, 11);
            if (d.length <= 3) { return d; }
            if (d.length <= 6) { return d.slice(0, 3) + '.' + d.slice(3); }
            if (d.length <= 9) { return d.slice(0, 3) + '.' + d.slice(3, 6) + '.' + d.slice(6); }
            return d.slice(0, 3) + '.' + d.slice(3, 6) + '.' + d.slice(6, 9) + '-' + d.slice(9);
        },
        telefone: function (valor) {
            const d = valor.replace(/\D/g, '').slice(0, 11);
            if (d.length === 0) { return ''; }
            if (d.length <= 2) { return '(' + d; }
            if (d.length <= 6) { return '(' + d.slice(0, 2) + ') ' + d.slice(2); }
            if (d.length <= 10) { return '(' + d.slice(0, 2) + ') ' + d.slice(2, 6) + '-' + d.slice(6); }
            return '(' + d.slice(0, 2) + ') ' + d.slice(2, 7) + '-' + d.slice(7);
        },
        cep: function (valor) {
            const d = valor.replace(/\D/g, '').slice(0, 8);
            return d.length > 5 ? d.slice(0, 5) + '-' + d.slice(5) : d;
        }
    };

    function iniciarMascaras() {
        document.querySelectorAll('input[data-mascara]').forEach(function (input) {
            const mascara = mascaras[input.getAttribute('data-mascara')];
            if (!mascara) {
                return;
            }
            const aplicar = function () { input.value = mascara(input.value); };
            input.addEventListener('input', aplicar);
            aplicar();
        });
    }

    /* ---------------------------------------------------------
       5. PRÉVIA DA IMAGEM ESCOLHIDA
       <input type="file" data-preview="idImg" data-icone="idIcone" data-nome="idSpan">
       --------------------------------------------------------- */
    const TAMANHO_MAXIMO = 2 * 1024 * 1024; // 2 MB (mesmo limite do application.properties)

    function iniciarUploads() {
        document.querySelectorAll('input[type="file"][data-preview]').forEach(function (input) {
            const previa = document.getElementById(input.getAttribute('data-preview'));
            const icone = document.getElementById(input.getAttribute('data-icone'));
            const nome = document.getElementById(input.getAttribute('data-nome'));

            input.addEventListener('change', function () {
                const arquivo = input.files && input.files[0];
                if (!arquivo) {
                    if (nome) { nome.textContent = 'Nenhum arquivo selecionado'; }
                    return;
                }
                if (arquivo.size > TAMANHO_MAXIMO) {
                    window.alert('A imagem deve ter no máximo 2 MB.');
                    input.value = '';
                    if (nome) { nome.textContent = 'Nenhum arquivo selecionado'; }
                    return;
                }
                if (nome) { nome.textContent = arquivo.name; }
                if (previa && arquivo.type.indexOf('image/') === 0) {
                    previa.src = URL.createObjectURL(arquivo);
                    previa.hidden = false;
                    if (icone) { icone.hidden = true; }
                }
            });
        });
    }

    /* ---------------------------------------------------------
       6. FECHAR ALERTAS (sucesso some sozinho depois de 7 s)
       --------------------------------------------------------- */
    document.addEventListener('click', function (evento) {
        const botao = evento.target.closest('.alert-close');
        if (botao) {
            botao.closest('.alert').remove();
        }
    });

    function fecharAlertasAutomaticamente() {
        setTimeout(function () {
            document.querySelectorAll('.alert[data-auto-fechar]').forEach(function (alerta) {
                alerta.remove();
            });
        }, 7000);
    }

    /* ---------------------------------------------------------
       7. ENVIAR O FORMULÁRIO AO MUDAR UM CAMPO
       <select data-auto-enviar> / <input data-auto-enviar>
       --------------------------------------------------------- */
    document.addEventListener('change', function (evento) {
        const campo = evento.target.closest('[data-auto-enviar]');
        if (campo && campo.form) {
            campo.form.submit();
        }
    });

    /* ---------------------------------------------------------
       8. BOTÕES + E - DE QUANTIDADE
       <div class="qtd-control"><button data-qtd="-1"> <input> <button data-qtd="1">
       --------------------------------------------------------- */
    document.addEventListener('click', function (evento) {
        const botao = evento.target.closest('[data-qtd]');
        if (!botao) {
            return;
        }
        const controle = botao.closest('.qtd-control');
        const input = controle ? controle.querySelector('input') : null;
        if (!input) {
            return;
        }
        const minimo = input.min ? parseInt(input.min, 10) : 1;
        const maximo = input.max ? parseInt(input.max, 10) : Infinity;
        let valor = (parseInt(input.value, 10) || 0) + parseInt(botao.getAttribute('data-qtd'), 10);
        valor = Math.max(minimo, Math.min(maximo, valor));
        if (String(valor) !== input.value) {
            input.value = String(valor);
            input.dispatchEvent(new Event('change', { bubbles: true }));
        }
    });

    /* ---------------------------------------------------------
       9. FILTRO DE OPÇÕES DE UM SELECT
       <input data-filtrar-select="idDoSelect"> filtra pelo texto da opção
       e pelo atributo data-busca (ex.: CPF sem pontuação)
       --------------------------------------------------------- */
    function iniciarFiltroDeSelect() {
        document.querySelectorAll('input[data-filtrar-select]').forEach(function (input) {
            const select = document.getElementById(input.getAttribute('data-filtrar-select'));
            if (!select) {
                return;
            }
            const opcoes = Array.prototype.slice.call(select.options);
            input.addEventListener('input', function () {
                const termo = semAcento(input.value.trim());
                const digitos = input.value.replace(/\D/g, '');
                opcoes.forEach(function (opcao) {
                    if (!opcao.value) {
                        return;
                    }
                    const texto = semAcento(opcao.textContent);
                    const extra = opcao.getAttribute('data-busca') || '';
                    const visivel = !termo || texto.indexOf(termo) !== -1
                        || (digitos.length > 0 && extra.indexOf(digitos) !== -1);
                    opcao.hidden = !visivel;
                    opcao.disabled = !visivel;
                });
            });
        });
    }

    /* ---------------------------------------------------------
       10. FILTROS DA LOJA RECOLHIDOS NO CELULAR
       <details data-fechar-no-celular open>
       --------------------------------------------------------- */
    function recolherNoCelular() {
        if (window.matchMedia('(max-width: 760px)').matches) {
            document.querySelectorAll('details[data-fechar-no-celular]').forEach(function (detalhes) {
                detalhes.open = false;
            });
        }
    }

    /* ---------------------------------------------------------
       INICIALIZAÇÃO
       --------------------------------------------------------- */
    document.addEventListener('DOMContentLoaded', function () {
        recolherNoCelular();
        iniciarAutocompletar();
        destacarTermos();
        iniciarMascaras();
        iniciarUploads();
        iniciarFiltroDeSelect();
        fecharAlertasAutomaticamente();
    });
})();
