import axios from 'axios';

const API_BASE_URL = 'http://localhost:6086/seat';

// 使用 POST 请求，参数放在请求体中
export const createTicket = (userId, seatId, eventId) => {
    return axios.post(`${API_BASE_URL}/seat/ticket/create`, { // 路径也更符合RESTful风格
        userId,
        seatId,
        eventId // 添加节目ID
    });
};
// 调用示例
createTicket(123, 456, 789) // userId, seatId, eventId
    .then(response => {
        // 处理成功响应
        console.log('订票成功:', response.data);
    })
    .catch(error => {
        // 处理错误
        console.error('订票失败:', error);
    });

export const releaseTicket = (seatId) => {
    return axios.get(`${API_BASE_URL}/relese`, {
        params: {
            seatId
        }
    });
};