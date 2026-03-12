export function maskPhone(phone, role) {
  if (!phone) return "-";

  if (role === "admin") return phone;

  return phone.replace(/^(\d{3})\d{4}(\d{4})$/, "$1****$2");
}
