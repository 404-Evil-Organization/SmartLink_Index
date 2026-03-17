/**
 * UTC 时间转换工具
 * @param {string} utcDateStr - UTC 时间字符串（例如 "2026-03-17T15:44:52.000+00:00"）
 * @returns {object} 包含各种格式的方法，当输入无效时所有方法返回空字符串
 */
export function createTimeConverter(utcDateStr) {
  const date = new Date(utcDateStr);
  const isValid = !isNaN(date.getTime());

  // 无效输入时返回“虚拟”对象，所有方法返回空字符串（或 null）
  if (!isValid) {
    return {
      toDate: () => null,
      toLocaleString: () => "",
      toLocaleDateString: () => "",
      toLocaleTimeString: () => "",
      toISOString: () => "",
      toLocalYMD: () => "",
    };
  }

  // 有效输入时返回正常转换对象
  return {
    /** 返回原生的 Date 对象 */
    toDate: () => date,

    /** 返回本地化的日期时间字符串 */
    toLocaleString: (options) => date.toLocaleString(undefined, options),

    /** 返回本地化的日期字符串（不含时间） */
    toLocaleDateString: (options) =>
      date.toLocaleDateString(undefined, options),

    /** 返回本地化的时间字符串（不含日期） */
    toLocaleTimeString: (options) =>
      date.toLocaleTimeString(undefined, options),

    /** 返回 ISO 字符串（保持 UTC） */
    toISOString: () => date.toISOString(),

    /** 返回 YYYY-MM-DD格式时间字符串 */
    toLocalYMD: () => {
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, "0");
      const day = String(date.getDate()).padStart(2, "0");
      return `${year}-${month}-${day}`;
    },
  };
}
