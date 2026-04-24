# TerminalAI 

TerminalAI é um cliente de chat offline para Android que permite interagir com modelos de linguagem de larga escala (LLMs) diretamente no dispositivo. Utilizando o formato GGUF, o app realiza inferência local, garantindo privacidade absoluta e disponibilidade total sem depender de APIs pagas ou conexão com a internet.

## Destaques Técnicos
Modelo: Suporte nativo para a família Qwen 2.5 (executando a versão 1.5B Instruct com quantização Q4_K_M).

Inferência Local: Utiliza a engine de inferência para processar bilhões de parâmetros em tempo real na CPU/GPU do celular.

Privacidade por Design: Nenhuma mensagem ou dado de contexto é enviado para a nuvem.

Performance Mobile: Otimizado para rodar de forma eficiente em dispositivos com arquitetura ARM.

## Stack Tecnológica
Android SDK: Desenvolvido em Kotlin/Java.

Formato de Modelo: GGUF (via llama.cpp/inference engine).

Gerenciamento de Arquivos: Integração com a pasta assets para carregamento dinâmico de modelos.

## AI & Human Collaboration
Grande parte deste repositório foi gerada e refinada com o auxílio de IA. Meu papel como desenvolvedor foi:

Arquitetura: Definir como os componentes do Android conversariam com o modelo GGUF.

Prompt Engineering: Elaborar instruções precisas para gerar blocos de código funcionais.

Debug & Validação: Testar, corrigir alocações de memória e garantir que a integração com o llama.cpp funcionasse no hardware móvel.

## Instalação e Uso
O modelo de IA (.gguf) não está presente no repositório devido ao seu tamanho (>1GB). Para rodar o TerminalAI:

Clone o repositório.

Baixe o arquivo do modelo qwen2.5-1.5b-instruct-q4_k_m.gguf.

Mova o arquivo para app/src/main/assets/.

Compile e instale o APK via Android Studio.

## Roadmap Pessoal
[ ] Suporte para outros modelos (Llama 3, Phi-3).

[ ] Implementação de histórico de conversas (SQLite/Room).

[ ] Melhoria na velocidade de tokenização.

[ ] Suporte a troca dinâmica de modelos via interface.

/app/src/main/assets: Local onde o modelo .gguf deve ser inserido.

/app/src/main/res: Recursos de layout, strings e estilos do aplicativo.
