/**
 * 手机号脱敏处理
 * @param {string|number} phone 手机号
 * @param {string} role 用户角色（'admin' 时不脱敏）
 * @returns {string}
 */
export function maskPhone(phone, role) {
  if (!phone) return '-';
  if (role === 'admin') return String(phone);

  const phoneStr = String(phone);
  // 提取所有数字
  const digits = phoneStr.replace(/\D/g, '');

  // 标准11位手机号处理：保留前3位、后4位，中间4位用星号替换
  if (digits.length === 11) {
    return digits.replace(/^(\d{3})\d{4}(\d{4})$/, '$1****$2');
  }

  // 非标准格式兜底处理
  if (digits.length === 0) {
    return '****'; // 没有数字，直接返回掩码
  }

  if (digits.length <= 4) {
    return '*'.repeat(digits.length); // 全部掩码
  }

  // 保留前最多3位和最后2位，中间掩码
  const headLen = Math.min(3, digits.length - 2);
  const head = digits.slice(0, headLen);
  const tail = digits.slice(-2);
  const maskedMiddle = '*'.repeat(digits.length - headLen - 2);
  return head + maskedMiddle + tail;
}