<?php
/**
 * Floating chat widget.
 *
 * Renders a bottom-right direct-message widget for logged-in users.
 * Messages are stored in the shared `messages` table using the columns
 * `incoming_msg_id`, `outgoing_msg_id`, and `msg`, similar to the ChatApp.
 *
 * @package TalentBridge
 */

if (!isset($_SESSION)) {
    session_start();
}

require_once __DIR__ . '/helpers.php';
require_once __DIR__ . '/auth.php';

// Only render the widget for authenticated users.
if (!isLoggedIn()) {
    return;
}
?>

<style>
    .tb-chat-widget {
        position: fixed;
        right: 1.5rem;
        bottom: 1.5rem;
        z-index: 1050;
        font-family: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
    }
    .tb-chat-toggle {
        border-radius: 999px;
        border: none;
        background: var(--tb-primary, #0d6efd);
        color: #fff;
        padding: 0.5rem 1.1rem;
        font-size: 0.95rem;
        box-shadow: 0 0.35rem 0.8rem rgba(0, 0, 0, 0.25);
        cursor: pointer;
    }
    .tb-chat-panel {
        width: 320px;
        max-width: 85vw;
        height: 420px;
        background: #fff;
        border-radius: 0.75rem;
        box-shadow: 0 0.6rem 1.5rem rgba(0, 0, 0, 0.2);
        display: none;
        flex-direction: column;
        overflow: hidden;
    }
    .tb-chat-panel.tb-chat-panel--open {
        display: flex;
    }
    .tb-chat-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 0.6rem 0.8rem;
        background: var(--tb-primary, #0d6efd);
        color: #fff;
        font-size: 0.9rem;
    }
    .tb-chat-header-title {
        font-weight: 600;
    }
    .tb-chat-close {
        border: none;
        background: transparent;
        color: inherit;
        font-size: 1.1rem;
        line-height: 1;
        cursor: pointer;
    }
    .tb-chat-partner {
        padding: 0.5rem 0.8rem;
        border-bottom: 1px solid #f0f0f0;
        font-size: 0.8rem;
        background-color: #fafafa;
    }
    .tb-chat-search {
        padding: 0.5rem 0.8rem;
        border-bottom: 1px solid #f0f0f0;
        background-color: #f9f9f9;
    }
    .tb-chat-search input[type="search"] {
        width: 100%;
        min-width: 0;
        box-sizing: border-box;
        padding: 0.35rem 0.5rem;
        border: 1px solid #ced4da;
        border-radius: 0.35rem;
    }
    .tb-chat-partner label {
        display: block;
        margin-bottom: 0.15rem;
        color: #555;
    }
    .tb-chat-partner input[type="number"] {
        width: 100%;
        font-size: 0.8rem;
        padding: 0.25rem 0.4rem;
        border-radius: 0.25rem;
        border: 1px solid #d0d0d0;
    }
    .tb-chat-contacts {
        padding: 0.5rem 0.8rem;
        border-bottom: 1px solid #f0f0f0;
        background-color: #f9f9f9;
        font-size: 0.8rem;
        max-height: 120px;
        overflow-y: auto;
    }
    .tb-chat-contacts-title {
        font-weight: 600;
        margin-bottom: 0.3rem;
        font-size: 0.82rem;
    }
    .tb-chat-user-item {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 0.35rem;
        padding: 0.25rem 0.3rem;
        border-radius: 0.35rem;
        background: #ffffff;
        border: 1px solid #e4e7eb;
    }
    .tb-chat-user-actions {
        display: flex;
        gap: 0.2rem;
    }
    .tb-chat-user-action {
        border: none;
        border-radius: 0.25rem;
        padding: 0.2rem 0.35rem;
        font-size: 0.72rem;
        cursor: pointer;
    }
    .tb-chat-user-action-chat {
        background: #0d6efd;
        color: #fff;
    }
    .tb-chat-user-action-delete {
        background: #dc3545;
        color: #fff;
    }
    .tb-chat-messages {
        flex: 1;
        padding: 0.6rem 0.6rem 0.4rem;
        overflow-y: auto;
        background: #f8f9fa;
        font-size: 0.85rem;
    }
    .tb-chat-empty {
        color: #888;
        font-size: 0.8rem;
        text-align: center;
        margin-top: 1.5rem;
    }
    .tb-chat-msg {
        max-width: 80%;
        margin-bottom: 0.4rem;
        padding: 0.35rem 0.55rem;
        border-radius: 0.75rem;
        clear: both;
        word-wrap: break-word;
    }
    .tb-chat-msg-author {
        font-size: 0.7rem;
        margin-bottom: 0.1rem;
        opacity: 0.8;
    }
    .tb-chat-msg-text {
        font-size: 0.83rem;
    }
    .tb-chat-msg--out {
        margin-left: auto;
        background: var(--tb-primary, #0d6efd);
        color: #fff;
        border-bottom-right-radius: 0.15rem;
    }
    .tb-chat-msg--in {
        margin-right: auto;
        background: #fff;
        border: 1px solid #e2e6ea;
        border-bottom-left-radius: 0.15rem;
    }
    .tb-chat-form {
        display: flex;
        padding: 0.45rem 0.45rem 0.5rem;
        border-top: 1px solid #e2e6ea;
        background: #fff;
        gap: 0.35rem;
    }
    .tb-chat-input {
        flex: 1;
        border-radius: 999px;
        border: 1px solid #ced4da;
        padding: 0.35rem 0.7rem;
        font-size: 0.85rem;
    }
    .tb-chat-send {
        border-radius: 999px;
        border: none;
        background: var(--tb-primary, #0d6efd);
        color: #fff;
        padding: 0.35rem 0.9rem;
        font-size: 0.85rem;
        cursor: pointer;
    }
    .tb-chat-send:disabled {
        opacity: 0.6;
        cursor: not-allowed;
    }
    @media (max-width: 575.98px) {
        .tb-chat-panel {
            width: 100vw;
            right: 0;
        }
        .tb-chat-widget {
            right: 0.75rem;
            bottom: 0.75rem;
        }
    }
</style>

<div class="tb-chat-widget">
    <button type="button" class="tb-chat-toggle" aria-expanded="false" aria-controls="tb-chat-panel">
        Chat
    </button>
    <div id="tb-chat-panel" class="tb-chat-panel" role="region" aria-label="Direct messages">
        <div class="tb-chat-header">
            <span class="tb-chat-header-title">Direct Messages</span>
            <button type="button" class="tb-chat-close" aria-label="Close chat">&times;</button>
        </div>
        <div class="tb-chat-search">
            <input id="tb-chat-search" type="search" placeholder="Search users..." aria-label="Search users">
        </div>
        <div class="tb-chat-contacts" id="tb-chat-contacts" aria-live="polite">
            <div id="tb-chat-user-list" class="tb-chat-user-list">Loading users...</div>
        </div>
        <div id="tb-chat-partner" class="tb-chat-partner">Select a user to start chatting</div>
        <div class="tb-chat-messages" id="tb-chat-messages">
            <div class="tb-chat-empty">No conversation selected.</div>
        </div>
        <form id="tb-chat-form" class="tb-chat-form">
            <input
                type="text"
                id="tb-chat-input"
                name="message"
                class="tb-chat-input"
                aria-label="Message"
                placeholder="Type a message..."
                autocomplete="off"
            >
            <button type="submit" class="tb-chat-send" disabled>Send</button>
        </form>
    </div>
</div>

<script>
(function () {
    const widget = document.querySelector('.tb-chat-widget');
    if (!widget) return;

    const toggleBtn = widget.querySelector('.tb-chat-toggle');
    const panel = widget.querySelector('.tb-chat-panel');
    const closeBtn = widget.querySelector('.tb-chat-close');
    const searchInput = widget.querySelector('#tb-chat-search');
    const userList = widget.querySelector('#tb-chat-user-list');
    const messagesEl = widget.querySelector('#tb-chat-messages');
    const form = widget.querySelector('.tb-chat-form');
    const input = widget.querySelector('.tb-chat-input');
    const sendBtn = widget.querySelector('.tb-chat-send');
    const partnerDisplay = widget.querySelector('#tb-chat-partner');

    let partnerId = null;
    let pollTimer = null;
    let availableUsers = [];
    let polling = false;
    let pollPending = false;

    function queueFetch() {
        if (pollPending) return;
        pollPending = true;
        setTimeout(function () {
            pollPending = false;
            fetchMessages();
        }, 1000);
    }

    function startPolling() {
        stopPolling();
        if (!partnerId) return;
        fetchMessages();
        pollTimer = window.setInterval(function () {
            fetchMessages();
        }, 1000);
    }

    function stopPolling() {
        if (pollTimer) {
            window.clearInterval(pollTimer);
            pollTimer = null;
        }
    }

    function setPanelOpen(open) {
        if (open) {
            panel.classList.add('tb-chat-panel--open');
            toggleBtn.setAttribute('aria-expanded', 'true');
            toggleBtn.style.display = 'none';
            // move focus into the panel so keyboard users can immediately interact
            input.focus();
            if (partnerId) {
                startPolling();
            }
        } else {
            panel.classList.remove('tb-chat-panel--open');
            toggleBtn.setAttribute('aria-expanded', 'false');
            toggleBtn.style.display = 'inline-flex';
            // return focus to the toggle button when the panel closes
            toggleBtn.focus();
            stopPolling();
        }
    }

    function renderUserList() {
        if (!Array.isArray(availableUsers) || availableUsers.length === 0) {
            userList.innerHTML = '<div class="tb-chat-empty">No other users found.</div>';
            return;
        }

        const term = (searchInput.value || '').trim().toLowerCase();
        const filtered = availableUsers.filter(user => user.name.toLowerCase().includes(term));

        if (filtered.length === 0) {
            userList.innerHTML = '<div class="tb-chat-empty">No users found.</div>';
            return;
        }

        userList.innerHTML = '';

        filtered.forEach(function (user) {
            const item = document.createElement('div');
            item.className = 'tb-chat-user-item';
            item.innerHTML = '' +
                '<div><div>'+ sanitiseText(user.name) +'</div><small style="font-size:.72rem;color:#6c757d;">'+ sanitiseText(user.status || 'Offline now') +'</small></div>' +
                '<button type="button" class="tb-chat-user-action tb-chat-user-action-chat" data-partnerid="' + user.unique_id + '">Chat</button>';

            const chatBtn = item.querySelector('.tb-chat-user-action-chat');

            chatBtn.addEventListener('click', function () {
                setChatPartner(user.unique_id, user.name);
            });

            userList.appendChild(item);
        });
    }

    function setChatPartner(id, name) {
        partnerId = id;
        sendBtn.disabled = false;
        partnerDisplay.textContent = 'Chatting with ' + sanitiseText(name);
        fetchMessages();
        if (!panel.classList.contains('tb-chat-panel--open')) {
            setPanelOpen(true);
        }
    }

    function loadUsers() {
        fetch('chat_users.php', {
            method: 'GET',
            credentials: 'same-origin'
        })
            .then(function (res) {
                if (!res.ok) throw new Error('HTTP ' + res.status);
                return res.json();
            })
            .then(function (users) {
                availableUsers = Array.isArray(users) ? users : [];
                renderUserList();
            })
            .catch(function () {
                userList.innerHTML = '<div class="tb-chat-empty">Unable to load users.</div>';
            });
    }


    function sanitiseText(text) {
        const el = document.createElement('span');
        el.textContent = text;
        return el.innerHTML;
    }

    function encodeForm(data) {
        return Object.keys(data)
            .map(function (key) {
                return encodeURIComponent(key) + '=' + encodeURIComponent(data[key]);
            })
            .join('&');
    }

    function fetchMessages() {
        if (!partnerId || polling) return;
        polling = true;

        fetch('/chat_fetch.php', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            credentials: 'same-origin',
            body: encodeForm({ incoming_id: partnerId })
        })
            .then(function (res) {
                return res.text().then(function (text) {
                    if (!res.ok) {
                        throw new Error('HTTP ' + res.status + ': ' + text);
                    }
                    return text;
                });
            })
            .then(function (html) {
                messagesEl.innerHTML = html;
                messagesEl.scrollTop = messagesEl.scrollHeight;
            })
            .catch(function (err) {
                console.error('chat_fetch error', err);
                messagesEl.innerHTML = '<div class="tb-chat-empty">Unable to load messages right now.</div><div style="font-size:.7rem;color:#a00;">' + (err.message || '') + '</div>';
            })
            .finally(function () {
                polling = false;
                queueFetch();
            });
    }

    function sendMessage(message) {
        if (!partnerId || !message) return;
        sendBtn.disabled = true;
        fetch('/chat_send.php', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            credentials: 'same-origin',
            body: encodeForm({ incoming_id: partnerId, message: message })
        })
            .then(function (res) {
                return res.text().then(function (text) {
                    if (!res.ok) {
                        throw new Error('HTTP ' + res.status + ': ' + text);
                    }
                    return text;
                });
            })
            .then(function () {
                input.value = '';
                fetchMessages();
            })
            .catch(function (err) {
                console.error('chat_send error', err);
            })
            .finally(function () {
                sendBtn.disabled = false;
            });
    }

    toggleBtn.addEventListener('click', function () {
        const isOpen = panel.classList.contains('tb-chat-panel--open');
        setPanelOpen(!isOpen);
    });

    closeBtn.addEventListener('click', function () {
        setPanelOpen(false);
    });

    searchInput.addEventListener('input', function () {
        renderUserList();
    });

    form.addEventListener('submit', function (e) {
        e.preventDefault();
        const message = input.value.trim();
        if (!partnerId || !message) return;
        sendMessage(message);
    });

    loadUsers();
})();
</script>

