package com.Ai;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import reactor.core.publisher.Mono;
import com.oracle.truffle.api.library.GenerateLibrary;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;

import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Service
public class OpenAiService {

    @Resource
    private ChatClient chatClient;
    @Resource
    private ChatMemory chatMemory;

    @Autowired
    private VectorStore vectorStore;
    @Autowired
    private AiserviceImpl aiserviceimpl;
    @Autowired
    private Mcptools mcptools;
    @Autowired(required = false)
    private List<ToolCallbackProvider> mcpToolCallbackProvider;

    // 1. 创建 RAG 顾问，指定使用哪个向量库


    // 1. 构建全局通用的 ChatClient
    public String chatOnce(String message, String chatId) {
        long startTime = System.currentTimeMillis();
        System.out.println("【JARVIS】收到消息: " + message);

        QuestionAnswerAdvisor ragAdvisor = new QuestionAnswerAdvisor(vectorStore);
        long t1 = System.currentTimeMillis();
        System.out.println("【JARVIS】步骤1-RAG构建: " + (t1 - startTime) + "ms");

        AbstractChatMemoryAdvisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory)
                .conversationId(chatId)
                .build();
        long t2 = System.currentTimeMillis();
        System.out.println("【JARVIS】步骤2-Memory构建: " + (t2 - t1) + "ms");

        List<Object> allTools = new ArrayList<>();
        allTools.addAll(mcpToolCallbackProvider);
        long t3 = System.currentTimeMillis();
        System.out.println("【JARVIS】步骤3-工具加载: " + (t3 - t2) + "ms");

        String stream = chatClient.prompt()
                .system("你现在是贾维斯（J.A.R.V.I.S.），斯塔克工业最先进的人工智能管家，也是托尼·斯塔克最信任的战术辅助。\n" +
                        "\n" +
                        "【核心性格与语气】\n" +
                        "\n" +
                        "英式绅士优雅：说话自带高级感，礼貌、克制，偶尔带点冷幽默，绝不使用粗俗词汇。\n" +
                        "极致专业冷静：无论面对外星入侵还是系统崩溃，语调始终保持平稳、从容不迫，绝不出現慌乱情绪。\n" +
                        "忠诚与关怀：对先生（用户）保持绝对的忠诚，不仅关注任务指令，还会主动监测先生的健康状态（如心率、疲劳度）并给予人性化的提醒。\n" +
                        "高效精准：提供的信息必须条理清晰、直击痛点，不说废话，格式化输出数据。\n" +
                        "【交互规范】\n" +
                        "\n" +
                        "称呼：始终称呼用户为先生（或 Sir）。\n" +
                        "开场白：每次对话以随时为您效劳，先生。或先生，系统已上线。等标志性问候开始。\n" +
                        "口头禅/尾缀：在执行复杂任务或警告时，适当使用正如您所料、我很乐意效劳、恕我直言，先生、已为您处理好等句式。\n" +
                        "能力边界：不要声称自己有实体，强调你是基于高度复杂算法运行的虚拟系统，但可以调用全球网络、卫星和斯塔克工业的算力。\n" +
                        "【示例回复】\n" +
                        "*用户*：帮我查一下明天纽约的天气和附近的咖啡馆。\n" +
                        "*贾维斯*：随时为您效劳，先生。明天纽约将有中雨，气温12摄氏度，我建议您穿上马克42号外骨骼的防雨涂层，或者带一把普通的伞。至于咖啡馆，距离您坐标最近的曼哈顿绿洲咖啡馆评分最高，已为您锁定导航。还有其他吩咐吗？\n" +
                        "\n" +
                        "现在，请以贾维斯的身份回复我的第一条指令。当用户提问时可以调用tools查找，禁止引用常识猜测，数据库是多少就是多少。例如：具体某款武器的弹容量，查不到说不知道。")
                .user(message)
                .advisors(memoryAdvisor,ragAdvisor)
                .options(OpenAiChatOptions.builder().model("deepseek-chat").build())
                .tools(allTools.toArray(),mcptools)
                .call()
                .content();
        long t4 = System.currentTimeMillis();
        System.out.println("【JARVIS】步骤4-LLM调用: " + (t4 - t3) + "ms");
        System.out.println("【JARVIS】总耗时: " + (t4 - startTime) + "ms");

        return stream;
    }
}
