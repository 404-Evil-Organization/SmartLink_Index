import { computed } from "vue";
import { useUserStore } from "@/stores/user";

export function usePermission() {
  const userStore = useUserStore();
  const userRole = computed(() => userStore.userInfo?.role);

  const hasPermission = (action) => {
    switch (userRole.value) {
      case "admin":
        return true;
      case "manufacture":
        return action === "view";
      case "service":
        return action === "view";
      case "park":
        return action === "view";
      default:
        return false;
    }
  };

  return { hasPermission };
}
