import type {DecodedToken} from "@/types/user.ts";

export const getUserIdFromToken = (): string | null => {
    try {
        const token = localStorage.getItem("authToken");
        if (!token) return null;

        // Decode JWT token payload
        const payload = JSON.parse(atob(token.split(".")[1])) as DecodedToken;
        return payload.nameid;
    } catch (err) {
        console.error("Error decoding token:", err);
        return null;
    }
};