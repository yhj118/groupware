/* 다크모드 */
const savedTheme = localStorage.getItem("theme") || "light";
document.documentElement.setAttribute("data-bs-theme", savedTheme);

function changeTheme(theme) {
    document.documentElement.setAttribute("data-bs-theme", theme);

    const currentTheme = document.getElementById("currentTheme");

    if (currentTheme) {
        currentTheme.textContent = theme === "dark" ? "다크 모드" : "라이트 모드";
    }
}

function saveTheme() {
    const selectedTheme = document.querySelector('input[name="theme"]:checked');

    if (!selectedTheme) {
        alert("화면 모드를 선택해주세요.");
        return;
    }

    localStorage.setItem("theme", selectedTheme.value);
    document.documentElement.setAttribute("data-bs-theme", selectedTheme.value);

    alert("설정이 저장되었습니다.");
}

document.addEventListener("DOMContentLoaded", function () {
    const theme = localStorage.getItem("theme") || "light";

    document.documentElement.setAttribute("data-bs-theme", theme);

    const radio = document.querySelector('input[name="theme"][value="' + theme + '"]');

    if (radio) {
        radio.checked = true;
    }

    const currentTheme = document.getElementById("currentTheme");

    if (currentTheme) {
        currentTheme.textContent = theme === "dark" ? "다크 모드" : "라이트 모드";
    }
});