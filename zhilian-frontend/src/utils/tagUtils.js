/**
 * 将任意格式的标签值，标准化为数组
 * @param {string | string[] | any} value
 * @returns {string[]}
 */
export const normalizeTags = (value) => {
  if (Array.isArray(value)) {
    // 对数组中每一项执行 String().trim() 并过滤空值，确保返回规范的 string[]
    return value
      .map((item) => (item == null ? "" : String(item).trim()))
      .filter((item) => item !== "");
  }

  if (value == null || value === "") {
    return [];
  }

  if (typeof value === "string") {
    return value
      .split(",")
      .map((item) => item.trim())
      .filter((item) => item !== "");
  }

  return String(value)
    .split(",")
    .map((item) => item.trim())
    .filter((item) => item !== "");
};

/**
 * 将标签数组组合为字符串
 * @param {string[] | any} tags
 * @returns {string}
 */
export const joinTags = (tags) => {
  if (!Array.isArray(tags)) {
    return "";
  }

  return tags
    .map((item) => (item == null ? "" : String(item).trim()))
    .filter((item) => item !== "")
    .join(",");
};
