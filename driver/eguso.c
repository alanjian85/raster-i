#include <linux/device.h>
#include <linux/module.h>
#include <linux/slab.h>
#include <linux/printk.h>
#include <asm/io.h>

MODULE_LICENSE("Dual MIT/GPL");

#define DEVICE_NAME "eguso"

static int width = 1024, height = 720;
module_param(width, int, 0);
module_param(height, int, 0);

static void *reg_base;
static char *framebuffer;

static int eguso_open(struct inode *inode, struct file *filp) {
    framebuffer = kmalloc(width * height * 3, GFP_KERNEL);
    while (!(readl(reg_base) & 4));
    while (!(readl(reg_base + 0x10000) & 4));
    writel(0, reg_base + 0x04);
    writel(height, reg_base + 0x10);
    writel(width, reg_base + 0x18);
    writel(9, reg_base + 0x20);
    writel(1, reg_base + 0x28);
    writel(0, reg_base + 0x30);
    writel(0, reg_base + 0x40);
    writel(50, reg_base + 0x78);
    writel(0x00, reg_base + 0x80);
    writel(0x63, reg_base + 0x88);
    writel(0x74, reg_base + 0x90);
    writel(0x81, reg_base);
    writel(0, reg_base + 0x10004);
    writel(width, reg_base + 0x10010);
    writel(height, reg_base + 0x10018); 
    writel(width * 3, reg_base + 0x10020);
    writel(20, reg_base + 0x10028);
    writeq(virt_to_phys(framebuffer), reg_base + 0x10030);
    writel(0x81, reg_base + 0x10000);
    return 0;
}

static int eguso_release(struct inode *inode, struct file *filp) {
    writel(0, reg_base);
    writel(0, reg_base + 0x10000);
    kfree(framebuffer);
    return 0;
}

static ssize_t eguso_read(struct file *filp,
                          char __user *buffer,
                          size_t length,
                          loff_t *offset)
{
    *offset += length;
    return copy_to_user(buffer, framebuffer, length);
}

static struct file_operations eguso_fops = {
    .open = eguso_open,
    .release = eguso_release,
    .read = eguso_read,
};

static int major;
static struct class *class;

static int __init eguso_init(void) {
    major = register_chrdev(0, DEVICE_NAME, &eguso_fops);
    class = class_create(THIS_MODULE, DEVICE_NAME);
    device_create(class, NULL, MKDEV(major, 0), NULL, DEVICE_NAME);
    reg_base = ioremap(0xa0000000, 0x20000);
    return 0;
}

static void __exit eguso_exit(void) {
    iounmap(reg_base);
    device_destroy(class, MKDEV(major, 0));
    class_destroy(class);
    unregister_chrdev(major, DEVICE_NAME);
}

module_init(eguso_init);
module_exit(eguso_exit);
