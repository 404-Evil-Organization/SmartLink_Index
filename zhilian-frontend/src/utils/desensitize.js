export function maskPhone(phone, role) {
  if (!phone) return "-";

  if (role === "admin") return phone;

  // 非管理员用户进行手机号脱敏：
  // 1. 优先按标准 11 位数字手机号（如 13812345678）进行脱敏：保留前三位和后四位，中间四位使用星号。
  // 2. 若不满足标准格式（如包含区号、空格、分隔符或为短号），则执行安全兜底逻辑：
  //    - 提取其中的数字做部分保留，其余全部使用星号替换，避免明文展示完整号码。
  const phoneStr = String(phone);
  // 标准 11 位手机号场景，保持原有行为不变
  if (/^(\d{3})\d{4}(\d{4})$/.test(phoneStr)) {
    return phoneStr.replace(/^(\d{3})\d{4}(\d{4})$/, "$1****$2");
  }
  // 安全兜底：处理包含区号/短号/分隔符等非常规格式
  const digits = phoneStr.replace(/\D/g, "");
  // 若未能提取到任何数字，直接返回通用掩码，避免返回原文
  if (!digits) {
    return "****";
  }
  // 数字位数较少时，全部使用星号覆盖
  if (digits.length <= 4) {
    return "*".repeat(digits.length);
  }
  // 数字位数足够时：保留前最多 3 位和最后 2 位，其余用星号替换
  const headLen = Math.min(3, digits.length - 4);
  const head = digits.slice(0, headLen);
  const tail = digits.slice(-2);
  const maskedMiddle = "*".repeat(digits.length - headLen - tail.length);
  return head + maskedMiddle + tail;
}
