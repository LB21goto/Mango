import axios from 'axios';

const AI_BASE = '';

export async function sendMessageToAi(message, options = {}) {
    try {
        const url = (AI_BASE ? AI_BASE : '') + '/api/ai/chat';
        console.log('我马上要发给后端的chatId 是:', options.sessionId);

        const resp = await axios.post(url, {
            message,
            chatId: options.sessionId || null
        }, {});

        if (resp.data) {
            if (typeof resp.data === 'string') return resp.data;
            if (resp.data.text) return { text: resp.data.text };
            if (resp.data.data && resp.data.data.text) return { text: resp.data.data.text };
            return resp.data;
        }
        return null;
    } catch (err) {
        throw err;
    }
}

export async function sendMessageToAiStreamFetch(message, options = {}, onChunk, { timeoutMs } = {}) {
    const url = (AI_BASE ? AI_BASE : '') + '/api/ai/chat';
    const controller = new AbortController();
    let timeoutId;
    if (timeoutMs) timeoutId = setTimeout(() => controller.abort(), timeoutMs);

    const resp = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message, chatId: options.sessionId || null }),
        signal: controller.signal
    });

    if (!resp.ok) {
        if (timeoutId) clearTimeout(timeoutId);
        throw new Error('请求失败 ' + resp.status);
    }

    const reader = resp.body.getReader();
    const dec = new TextDecoder();
    let buf = '';

    try {
        while (true) {
            const { value, done } = await reader.read();
            if (done) break;

            buf += dec.decode(value, { stream: true });

            const lines = buf.split(/\r?\n/);
            buf = lines.pop() || '';

            for (const line of lines) {
                if (line.startsWith('data:')) {
                    const data = line.slice(5).trim();
                    if (data === '[DONE]') {
                        if (timeoutId) clearTimeout(timeoutId);
                        return;
                    }
                    if (data) {
                        try {
                            const parsed = JSON.parse(data);
                            const text = parsed.text ?? parsed.delta ?? parsed.content;
                            if (text) onChunk(text);
                        } catch {
                            if (data !== '[DONE]') onChunk(data);
                        }
                    }
                }
            }
        }

        if (buf.trim()) {
            const line = buf.trim();
            if (line.startsWith('data:')) {
                const data = line.slice(5).trim();
                if (data && data !== '[DONE]') {
                    try {
                        const parsed = JSON.parse(data);
                        const text = parsed.text ?? parsed.delta ?? parsed.content;
                        if (text) onChunk(text);
                    } catch {
                        onChunk(data);
                    }
                }
            }
        }
    } finally {
        if (timeoutId) clearTimeout(timeoutId);
    }
}