#!/bin/bash
# Team_Namoo classification-api EC2 세팅 스크립트 (도커 미사용)
# Amazon Linux 2023 / t3.micro 에서 ec2-user 로 실행.
# 사전 조건: 홈(~/)에 모델 폴더가 "model-latest" 이름으로 이미 올라와 있을 것
#            (로컬 맥에서 scp -r .../model/latest ec2-user@IP:/home/ec2-user/model-latest)
set -e

echo "== 1/6 스왑 4GB =="
if ! sudo swapon --show | grep -q /swapfile; then
  sudo dd if=/dev/zero of=/swapfile bs=1M count=4096 status=progress
  sudo chmod 600 /swapfile
  sudo mkswap /swapfile
  sudo swapon /swapfile
  echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
fi
echo 'vm.swappiness=10' | sudo tee /etc/sysctl.d/99-swap.conf >/dev/null
sudo sysctl -w vm.swappiness=10 >/dev/null
free -h

echo "== 2/6 필수 패키지 =="
sudo dnf install -y git python3 python3-pip

echo "== 3/6 코드 받기 =="
cd ~
[ -d Team_Namoo ] || git clone https://github.com/ursamajor0714/Team_Namoo.git
cd ~/Team_Namoo/classification-api

echo "== 4/6 모델 파일 배치 =="
mkdir -p model
if [ ! -f model/latest/config.json ]; then
  if [ -d ~/model-latest ]; then
    rm -rf model/latest
    mv ~/model-latest model/latest
  else
    echo "!! ~/model-latest 가 없습니다. 로컬 맥에서 먼저 scp 하세요:"
    echo "   scp -i ~/.ssh/team-namoo-model.pem -r \\"
    echo "     ~/Developer/Team_Namoo/classification-api/model/latest \\"
    echo "     ec2-user@<이 서버 IP>:/home/ec2-user/model-latest"
    exit 1
  fi
fi
ls -la model/latest

echo "== 5/6 파이썬 가상환경 + 설치 (torch는 CPU 전용 휠) =="
python3 -m venv .venv
./.venv/bin/pip install --upgrade pip
./.venv/bin/pip install torch --index-url https://download.pytorch.org/whl/cpu
./.venv/bin/pip install fastapi "uvicorn[standard]" transformers

echo "== 6/6 상시 실행 서비스 등록 =="
sudo tee /etc/systemd/system/namoo-model.service > /dev/null <<'UNIT'
[Unit]
Description=Team Namoo classification API
After=network.target

[Service]
User=ec2-user
WorkingDirectory=/home/ec2-user/Team_Namoo/classification-api
Environment=MODEL_DIR=/home/ec2-user/Team_Namoo/classification-api/model/latest
ExecStart=/home/ec2-user/Team_Namoo/classification-api/.venv/bin/uvicorn main:app --host 0.0.0.0 --port 8000
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
UNIT

sudo systemctl daemon-reload
sudo systemctl enable --now namoo-model
sleep 5
sudo systemctl status namoo-model --no-pager || true

echo
echo "=== 끝. 모델 로딩에 20~40초 걸립니다. 잠시 후 아래로 확인: ==="
echo "    curl localhost:8000/health      →  {\"status\":\"ok\"}"
echo "    journalctl -u namoo-model -f    →  로그 실시간"
