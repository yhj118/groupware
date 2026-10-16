(() => {
    'use strict';
    if (window.approvalEditReady) return;
    window.approvalEditReady = true;

    const initializedForms = new WeakSet();
    let fragmentRequest = null;
    let fragmentSequence = 0;

    function showError(root, message) {
        let output = root.querySelector('[data-edit-error]');
        if (!output) {
            output = document.createElement('p');
            output.className = 'text-danger p-3';
            output.setAttribute('role', 'alert');
            output.dataset.editError = '';
            root.prepend(output);
        }
        output.textContent = message;
        output.hidden = false;
    }

    function reindexItems(form) {
        const body = form.querySelector('[data-item-body]');
        if (!body) return;
        [...body.rows].forEach((row, index) => {
            row.querySelector('[data-item-number]').textContent = index + 1;
            row.querySelector('[data-item-name]').name = `items[${index}].itemName`;
            row.querySelector('[data-item-amount]').name = `items[${index}].itemAmount`;
            row.querySelector('[data-remove-item]').disabled = body.rows.length === 1;
        });
    }

    function calculateTotal(form) {
        const inputs = [...form.querySelectorAll('[data-item-amount]')];
        if (!inputs.length) return;
        let total = 0;
        inputs.forEach(input => {
            input.setCustomValidity('');
            const value = Number(input.value);
            if (Number.isFinite(value)) total += value;
        });
        const output = form.querySelector('[data-item-total]');
        const hidden = form.querySelector('[data-item-total-input]');
        output.textContent = total.toLocaleString('ko-KR') + ' 원';
        hidden.value = total;
        if (total > 100000000) {
            inputs[0].setCustomValidity('총 품의 금액은 1억 원 이하로 입력하세요.');
        }
    }

    function addItem(form) {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td data-item-number></td>
            <td><input type="text" class="form-control" data-item-name aria-label="물품 이름" required></td>
            <td>
                <div class="edit-money-input">
                    <input type="number" class="form-control" data-item-amount min="0" max="100000000" step="1" aria-label="물품 금액" required>
                    <span class="edit-money-unit">원</span>
                </div>
            </td>
            <td><button type="button" class="btn btn-sm btn-outline-danger" data-remove-item>삭제</button></td>`;
        form.querySelector('[data-item-body]').append(row);
        reindexItems(form);
        calculateTotal(form);
        row.querySelector('[data-item-name]').focus();
        row.scrollIntoView({ block: 'nearest' });
    }

    function syncPeriods(form) {
        [['vacationStart', 'vacationEnd'], ['businessTripStart', 'businessTripEnd'], ['workReportStart', 'workReportEnd']].forEach(([startName, endName]) => {
            const start = form.elements.namedItem(startName);
            const end = form.elements.namedItem(endName);
            if (!start || !end) return;
            end.min = start.value || '';
            end.setCustomValidity(start.value && end.value && end.value < start.value ? '종료일은 시작일보다 빠를 수 없습니다.' : '');
        });
    }

    function renderNewFiles(input) {
        const form = input.form;
        const list = form.querySelector('[data-new-file-list]');
        list.replaceChildren();
        const files = [...input.files];
        input.setCustomValidity(files.some(file => !/\.(jpg|jpeg|pdf|zip)$/i.test(file.name)) ? 'JPG, JPEG, PDF, ZIP 파일만 선택할 수 있습니다.' : '');
        files.forEach((file, index) => {
            const row = document.createElement('div');
            row.className = 'edit-new-file';
            const name = document.createElement('span');
            const size = file.size >= 1048576 ? (file.size / 1048576).toFixed(1) + ' MB' : (file.size / 1024).toFixed(1) + ' KB';
            name.textContent = file.name + ' · ' + size;
            const button = document.createElement('button');
            button.type = 'button';
            button.className = 'btn btn-sm btn-outline-danger';
            button.dataset.removeNewFile = index;
            button.textContent = '선택 취소';
            button.setAttribute('aria-label', file.name + ' 선택 취소');
            row.append(name, button);
            list.append(row);
        });
    }

    function initialize(root) {
        const forms = root.matches?.('[data-approval-edit-form]') ? [root] : root.querySelectorAll('[data-approval-edit-form]');
        forms.forEach(form => {
            if (initializedForms.has(form)) return;
            initializedForms.add(form);
            reindexItems(form);
            calculateTotal(form);
            syncPeriods(form);
            const files = form.querySelector('[data-new-attachments]');
            if (files) renderNewFiles(files);
        });
    }

    // 상세보기 팝업 안에서는 화면 이동 없이 수정 프래그먼트로 교체합니다.
    async function loadFragment(link, modal) {
        const holder = modal.querySelector('#approvalDetailContent');
        if (!holder) {
            window.location.assign(link.href);
            return;
        }
        fragmentRequest?.abort();
        const request = new AbortController();
        fragmentRequest = request;
        const sequence = ++fragmentSequence;
        const timeout = setTimeout(() => request.abort(), 15000);
        link.setAttribute('aria-busy', 'true');
        try {
            const response = await fetch(link.href, {
                headers: { 'X-Requested-With': 'XMLHttpRequest', 'Accept': 'text/html' },
                credentials: 'same-origin',
                signal: request.signal
            });
            if (response.redirected) throw new Error('로그인 상태 또는 조회 권한을 확인해주세요.');
            if (!response.ok) throw new Error(response.status === 403 ? '문서를 수정할 권한이 없습니다.' : '화면을 불러오지 못했습니다.');
            const markup = await response.text();
            const page = new DOMParser().parseFromString(markup, 'text/html');
            const content = page.querySelector('[data-approval-edit], [data-approval-detail]');
            if (!content) throw new Error('수정 화면의 프래그먼트를 확인해주세요.');
            if (sequence !== fragmentSequence || !modal.open || !link.isConnected) return;
            holder.replaceChildren(content);
            initialize(content);
            const titleId = modal.getAttribute('aria-labelledby')?.split(' ')[0];
            const title = titleId ? document.getElementById(titleId) : null;
            if (title) title.textContent = content.dataset.pageTitle || content.querySelector('.proposal-form-name')?.textContent || '문서 상세보기';
            const focusTarget = content.querySelector('input:not([type="hidden"]):not(:disabled), select:not(:disabled), textarea:not(:disabled), h2');
            if (focusTarget) {
                if (focusTarget.tagName === 'H2') focusTarget.tabIndex = -1;
                focusTarget.focus({ preventScroll: true });
            }
            const scroll = holder.closest('.approval-modal-scroll');
            if (scroll) scroll.scrollTop = 0;
        } catch (error) {
            if (sequence === fragmentSequence && modal.open && link.isConnected) {
                showError(holder, error.name === 'AbortError' ? '화면을 불러오는 시간이 초과되었습니다. 다시 시도해주세요.' : error.message);
            }
        } finally {
            clearTimeout(timeout);
            link.removeAttribute('aria-busy');
            if (fragmentRequest === request) fragmentRequest = null;
        }
    }

    document.addEventListener('click', event => {
        if (!(event.target instanceof Element)) return;
        const link = event.target.closest('a.approval-edit-button, a[data-edit-cancel]');
        if (link && event.button === 0 && !event.ctrlKey && !event.metaKey && !event.shiftKey && !event.altKey) {
            const modal = link.closest('dialog');
            if (modal?.open) {
                event.preventDefault();
                loadFragment(link, modal);
                return;
            }
        }
        const button = event.target.closest('button');
        const form = button?.closest('[data-approval-edit-form]');
        if (!form || button.disabled || button.matches(':disabled')) return;
        if (button.hasAttribute('data-add-item')) addItem(form);
        if (button.hasAttribute('data-remove-item')) {
            const body = form.querySelector('[data-item-body]');
            if (body.rows.length <= 1) return;
            button.closest('tr').remove();
            reindexItems(form);
            calculateTotal(form);
        }
        if (button.hasAttribute('data-remove-new-file')) {
            const input = form.querySelector('[data-new-attachments]');
            const transfer = new DataTransfer();
            [...input.files].forEach((file, index) => {
                if (index !== Number(button.dataset.removeNewFile)) transfer.items.add(file);
            });
            input.files = transfer.files;
            renderNewFiles(input);
        }
    });

    document.addEventListener('input', event => {
        const form = event.target.closest?.('[data-approval-edit-form]');
        if (!form) return;
        if (event.target.matches('[data-item-amount]')) calculateTotal(form);
        if (event.target.type === 'date') syncPeriods(form);
    });

    document.addEventListener('change', event => {
        const form = event.target.closest?.('[data-approval-edit-form]');
        if (!form) return;
        if (event.target.matches('[data-new-attachments]')) renderNewFiles(event.target);
        if (event.target.matches('[data-delete-attachment]')) {
            event.target.closest('.edit-file-card').classList.toggle('is-marked', event.target.checked);
        }
        if (event.target.type === 'date') syncPeriods(form);
    });

    document.addEventListener('submit', event => {
        const form = event.target;
        if (form.matches('[data-approval-delete-form]')) {
            if (form.dataset.submitting === 'true') {
                event.preventDefault();
                return;
            }
            if (!window.confirm('문서와 첨부파일, 구매 물품을 삭제하시겠습니까?')) {
                event.preventDefault();
                return;
            }
            form.dataset.submitting = 'true';
            const button = form.querySelector('button[type="submit"]');
            button.disabled = true;
            button.textContent = '삭제 중…';
            return;
        }
        if (!form.matches('[data-approval-edit-form]')) return;
        reindexItems(form);
        calculateTotal(form);
        syncPeriods(form);
        if (!form.checkValidity()) {
            event.preventDefault();
            form.reportValidity();
            return;
        }
        if (form.dataset.submitting === 'true') {
            event.preventDefault();
            return;
        }
        form.dataset.submitting = 'true';
        const button = form.querySelector('button[type="submit"]');
        button.disabled = true;
        button.textContent = '저장 중…';
        // 저장은 일반 POST입니다. 서버의 redirect:/openEAMain 응답으로 이동합니다.
    });

    function start() {
        initialize(document);
        new MutationObserver(records => {
            records.forEach(record => record.addedNodes.forEach(node => {
                if (node instanceof Element) initialize(node);
            }));
        }).observe(document.body, { childList: true, subtree: true });
        document.addEventListener('close', event => {
            if (event.target.tagName === 'DIALOG') {
                ++fragmentSequence;
                fragmentRequest?.abort();
            }
        }, true);
    }
    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', start, { once: true });
    else start();
})();
